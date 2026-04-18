package com.pharmacy.network;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;

@Slf4j
@RequiredArgsConstructor
public class RpcFactory {

    private final ServerConnection connection;

    @SuppressWarnings("unchecked")
    public <T> T createService(Class<T> serviceInterface) {
        log.info("Creating RPC proxy for service: {}", serviceInterface.getName());
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class<?>[]{serviceInterface},
                (proxy, method, args) -> {
                    String methodName = method.getName();
                    if (method.getDeclaringClass() == Object.class) {
                        switch (methodName) {
                            case "equals":
                                return proxy == args[0];
                            case "hashCode":
                                return System.identityHashCode(proxy);
                            case "toString":
                                return serviceInterface.getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
                            default:
                                throw new IllegalStateException("Not support method of Object: " + methodName);
                        }
                    }

                    log.debug("Invoking RPC method {}.{} with {} argument(s)",
                            serviceInterface.getSimpleName(),
                            methodName,
                            args == null ? 0 : args.length);

                    return connection.sendRequest(
                            serviceInterface.getSimpleName(),
                            methodName,
                            method.getParameterTypes(),
                            args
                    );
                }
        );
    }
}
