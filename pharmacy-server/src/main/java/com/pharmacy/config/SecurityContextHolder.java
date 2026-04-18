package com.pharmacy.config;

import java.util.HashSet;
import java.util.Set;

public class SecurityContextHolder {

    private static final ThreadLocal<Set<String>> currentPermissions = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Boolean> isAuthenticated = ThreadLocal.withInitial(() -> false);

    // Called when the user logs in successfully
    public static void setLoginSession(Set<String> permissions) {
        isAuthenticated.set(true);
        currentPermissions.get().clear();
        if (permissions != null) {
            currentPermissions.get().addAll(permissions);
        }
    }

    public static Set<String> getPermissions() {
        return currentPermissions.get();
    }

    public static boolean isAuthenticated() {
        return isAuthenticated.get();
    }

    // Always call this method when the Socket closes to prevent Memory Leaks
    public static void clear() {
        currentPermissions.remove();
        isAuthenticated.remove();
    }
}
