package com.pharmacy.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class ClientSecurityContext {

    private static final Set<String> permissions = new HashSet<>();

    private ClientSecurityContext() {
    }

    public static synchronized void setPermissions(Set<String> newPermissions) {
        permissions.clear();
        if (newPermissions != null) {
            permissions.addAll(newPermissions);
        }
    }

    public static synchronized boolean hasPermission(String permissionKey) {
        return permissionKey != null && permissions.contains(permissionKey);
    }

    public static synchronized Set<String> getPermissions() {
        return Collections.unmodifiableSet(new HashSet<>(permissions));
    }

    public static synchronized void clear() {
        permissions.clear();
    }
}

