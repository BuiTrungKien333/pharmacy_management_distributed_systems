package com.pharmacy.repository;

import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthorizationRepository {

    public Set<String> findPermissionKeysForUser(EntityManager em, String username) {
        List<String> keys = em.createQuery(
                        "select distinct p.permissionKey " +
                                "from Employee e " +
                                "join e.roles r " +
                                "join r.permissions p " +
                                "where e.employeeCode = :username",
                        String.class)
                .setParameter("username", username)
                .getResultList();

        return keys.stream()
                .filter(k -> k != null && !k.isBlank())
                .collect(Collectors.toUnmodifiableSet());
    }
}

