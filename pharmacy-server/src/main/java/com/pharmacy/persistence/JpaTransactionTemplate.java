package com.pharmacy.persistence;

import com.pharmacy.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JpaTransactionTemplate {

    private JpaTransactionTemplate() {
    }

    public static <T> T execute(JpaTransactionCallback<T> callback) {
        EntityManager em = HibernateUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T result = callback.doInTransaction(em);
            if (tx.isActive()) {
                tx.commit();
            }
            return result;
        } catch (RuntimeException e) {
            rollbackQuietly(tx);
            throw e;
        } catch (Exception e) {
            rollbackQuietly(tx);
            throw new RuntimeException(e);
        } finally {
            try {
                em.close();
            } catch (Exception e) {
                log.debug("event=entity_manager_close_failed errorMessage={}", e.getMessage());
            }
        }
    }

    private static void rollbackQuietly(EntityTransaction tx) {
        try {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
        } catch (Exception e) {
            log.warn("event=transaction_rollback_failed errorMessage={}", e.getMessage());
        }
    }
}
