package com.pharmacy.util;

import com.pharmacy.network.RpcFactory;

import java.util.HashMap;
import java.util.Map;

public class ClientContext {

    private static RpcFactory rpcFactory;

    private static final Map<Class<?>, Object> serviceCache = new HashMap<>();

    public static void init(RpcFactory factory) {
        rpcFactory = factory;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getService(Class<T> serviceInterface) {
        if (rpcFactory == null) {
            throw new IllegalStateException("ClientContext chưa được khởi tạo!");
        }

        return (T) serviceCache.computeIfAbsent(
                serviceInterface,
                type -> rpcFactory.createService(type)
        );
    }
}