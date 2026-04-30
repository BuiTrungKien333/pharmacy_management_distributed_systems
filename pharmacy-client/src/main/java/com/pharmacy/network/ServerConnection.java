package com.pharmacy.network;

import com.pharmacy.shared.dto.request.RpcRequest;
import com.pharmacy.shared.dto.response.RpcResponse;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

public class ServerConnection {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    // Open connection to the Server
    public synchronized void connect(String host, int port) throws Exception {
        try {
            this.socket = new Socket();
            this.socket.connect(new InetSocketAddress(host, port), 5000);
            this.socket.setSoTimeout(15000);

            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (Exception e) {
            disconnect();
            throw new RuntimeException("Unable to connect to the server.: " + e.getMessage(), e);
        }
    }

    // Shared method to send Request and receive Response
    public synchronized Object sendRequest(String serviceName, String methodName, Class<?>[] paramTypes, Object[] args) {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            throw new IllegalStateException("No connection to server!");
        }

        try {
            RpcRequest request = new RpcRequest();
            request.setServiceName(serviceName);
            request.setMethodName(methodName);
            request.setParameterTypes(paramTypes);
            request.setParameters(args);

            // Ensure updated objects (e.g., Pagination) are re-serialized each call.
            out.reset();
            out.writeObject(request);
            out.flush();

            RpcResponse response = (RpcResponse) in.readObject();

            if (response.getStatus() == 200) {
                return response.getResult();
            }

            Exception ex = response.getException();
            if (ex instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (ex != null) {
                throw new RuntimeException(ex.getMessage(), ex);
            }
            throw new RuntimeException("Unknown server error with Status Code: " + response.getStatus());
        } catch (SocketException e) {
            disconnect();
            throw new RuntimeException("The connection to the server has been closed. Please try logging in again.", e);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("RPC call failed: " + methodName, e);
        }
    }

    public synchronized void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (Exception ignored) {
        } finally {
            in = null;
            out = null;
            socket = null;
        }
    }
}