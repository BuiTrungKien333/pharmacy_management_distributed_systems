package com.pharmacy.network;

import com.pharmacy.config.RequirePermissions;
import com.pharmacy.config.SecurityContextHolder;
import com.pharmacy.shared.dto.request.RpcRequest;
import com.pharmacy.shared.dto.response.RpcResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.EOFException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final Map<String, Object> serviceRegistry;

    @Override
    public void run() {
        String clientIp = clientSocket.getInetAddress().getHostAddress();
        int clientPort = clientSocket.getPort();

        log.info("event=client_session_start clientIp={} clientPort={}", clientIp, clientPort);

        try (ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {
            while (!clientSocket.isClosed()) {
                try {
                    // 1. Read Request via Serialization protocol
                    RpcRequest request = (RpcRequest) in.readObject();

                    if (log.isDebugEnabled()) {
                        log.debug("event=request_received clientIp={} clientPort={} service={} method={}",
                                clientIp, clientPort, request.getServiceName(), request.getMethodName());
                    }

                    RpcResponse response = new RpcResponse();

                    try {
                        // 2. Find Service
                        Object service = serviceRegistry.get(request.getServiceName());
                        if (service == null) {
                            throw new IllegalArgumentException("Unknown service: " + request.getServiceName());
                        }

                        // 3. Find Method using Reflection
                        Method method = service.getClass().getMethod(request.getMethodName(), request.getParameterTypes());

                        // 4. Delegate security check (Authorization Middleware)
                        checkAuthorization(service, method);

                        // 5. Execute business logic (Invoke)
                        Object result = method.invoke(service, request.getParameters());

                        response.setStatus(200); // OK
                        response.setResult(result);

                    } catch (SecurityException se) {
                        log.warn("event=unauthorized_access clientIp={} reason={}", clientIp, se.getMessage());
                        response.setStatus(403);
                        response.setException(new SecurityException(safeMessage(se.getMessage(), "Unauthorized request.")));
                    } catch (NoSuchMethodException ne) {
                        response.setStatus(404);
                        response.setException(new RuntimeException("Method not found: " + request.getMethodName()));
                    } catch (Exception e) {
                        Throwable cause = (e.getCause() != null) ? e.getCause() : e;
                        log.error("event=request_handling_failed clientIp={} errorType={} error={}",
                                clientIp,
                                cause.getClass().getSimpleName(),
                                safeMessage(cause.getMessage(), "Server error"));

                        if (cause instanceof IllegalArgumentException) {
                            response.setStatus(400);
                            response.setException(new IllegalArgumentException(
                                    safeMessage(cause.getMessage(), "Invalid request data.")));
                        } else if (cause instanceof SecurityException) {
                            response.setStatus(403);
                            response.setException(new SecurityException(
                                    safeMessage(cause.getMessage(), "Access denied.")));
                        } else {
                            response.setStatus(500);
                            response.setException(new RuntimeException(
                                    safeMessage(cause.getMessage(), "Internal server error.")));
                        }
                    }

                    out.writeObject(response);
                    out.flush();

                    if (log.isDebugEnabled()) {
                        log.debug("event=response_sent clientIp={} clientPort={} status={}",
                                clientIp, clientPort, response.getStatus());
                    }

                } catch (EOFException e) {
                    log.debug("event=client_eof clientIp={} clientPort={}", clientIp, clientPort);
                    break; // Client actively closed connection
                } catch (SocketTimeoutException e) {
                    // Idle connection: keep session alive and wait for next request.
                    if (log.isDebugEnabled()) {
                        log.debug("event=client_idle clientIp={} clientPort={} reason=read_timeout", clientIp, clientPort);
                    }
                    continue;
                } catch (ClassNotFoundException e) {
                    log.warn("event=invalid_serialization_format clientIp={} error={}", clientIp, e.getMessage());
                    sendError(out, 400, "Invalid object format received.");
                } catch (Exception e) {
                    log.warn("event=frame_read_failed clientIp={} errorMessage={}", clientIp, e.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("event=client_connection_terminated clientIp={} clientPort={}", clientIp, clientPort, e);
        } finally {
            SecurityContextHolder.clear();
            try {
                if (!clientSocket.isClosed()) {
                    clientSocket.close();
                }
            } catch (Exception e) {
                log.debug("event=client_socket_close_failed errorMessage={}", e.getMessage());
            }
        }
    }

    private void checkAuthorization(Object service, Method implMethod) throws SecurityException {
        RequirePermissions annotation = findRequirePermissionsAnnotation(service, implMethod);
        if (annotation == null) {
            return;
        }

        String[] requiredPerms = annotation.value();
        if (requiredPerms.length == 0) {
            return;
        }

        // 1. Check if logged in
        if (!SecurityContextHolder.isAuthenticated()) {
            throw new SecurityException("Unauthorized: You must login first.");
        }

        // 2. Get permissions of the current user
        Set<String> userPerms = SecurityContextHolder.getPermissions();

        // 3. Check each required permission (RBAC Check)
        for (String required : requiredPerms) {
            if (!userPerms.contains(required)) {
                throw new SecurityException("Forbidden: Missing permission [" + required + "]");
            }
        }
    }

    private RequirePermissions findRequirePermissionsAnnotation(Object service, Method implMethod) {
        // Prefer annotation on implementation method.
        RequirePermissions annotation = implMethod.getAnnotation(RequirePermissions.class);
        if (annotation != null) {
            return annotation;
        }

        // Fallback: support annotation declared on interface method.
        for (Class<?> contract : service.getClass().getInterfaces()) {
            try {
                Method interfaceMethod = contract.getMethod(implMethod.getName(), implMethod.getParameterTypes());
                annotation = interfaceMethod.getAnnotation(RequirePermissions.class);
                if (annotation != null) {
                    return annotation;
                }
            } catch (NoSuchMethodException ignored) {
                // This interface does not declare the method.
            }
        }

        return null;
    }

    private void sendError(ObjectOutputStream out, int status, String message) {
        try {
            RpcResponse response = new RpcResponse();
            response.setStatus(status);
            response.setException(new RuntimeException(message));
            out.writeObject(response);
            out.flush();
        } catch (Exception ignored) {
            // best-effort
        }
    }

    private String safeMessage(String message, String fallback) {
        return (message == null || message.isBlank()) ? fallback : message;
    }
}
