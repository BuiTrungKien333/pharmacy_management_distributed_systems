package com.pharmacy.persistence;

import jakarta.persistence.EntityManager;

@FunctionalInterface
public interface JpaTransactionCallback<T> {
    T doInTransaction(EntityManager em) throws Exception;
}
