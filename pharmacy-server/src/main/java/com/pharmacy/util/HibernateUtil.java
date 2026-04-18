package com.pharmacy.util;

import com.pharmacy.shared.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HibernateUtil {

    private static final String PERSISTENCE_UNIT_NAME = "pharmacy-pu";
    private static volatile boolean initialized = false;

    private HibernateUtil() {
    }

    private static class LazyHolder {
        static final EntityManagerFactory FACTORY;
        static final HikariDataSource DATA_SOURCE;

        static {
            HikariDataSource dataSource = null;
            EntityManagerFactory emf;
            try {
                log.info("Initializing JPA EntityManagerFactory (persistenceUnit={}).", PERSISTENCE_UNIT_NAME);

                dataSource = configureDataSource();
                Map<String, Object> jpaProps = new HashMap<>();
                jpaProps.put("jakarta.persistence.nonJtaDataSource", dataSource);

                emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, jpaProps);
            } catch (Exception e) {
                log.error("Failed to initialize JPA EntityManagerFactory. Server cannot start.", e);
                closeQuietly(dataSource);
                HibernateUtil.initialized = false;
                throw new ExceptionInInitializerError("Database connection failed during startup: " + e.getMessage());
            }
            FACTORY = emf;
            DATA_SOURCE = dataSource;
            HibernateUtil.initialized = true;
            log.info("JPA EntityManagerFactory initialized successfully.");
        }
    }

    private static void closeQuietly(HikariDataSource dataSource) {
        if (dataSource == null || dataSource.isClosed()) {
            return;
        }
        try {
            dataSource.close();
            log.info("Closed HikariCP datasource after failed EntityManagerFactory initialization.");
        } catch (Exception closeEx) {
            log.warn("event=hikari_close_failed_after_emf_error errorMessage={}", closeEx.getMessage());
        }
    }

    private static HikariDataSource configureDataSource() {
        String jdbcUrl = AppConfig.get("db.url");
        String username = AppConfig.get("db.user");
        String password = AppConfig.get("db.password");

        int maxPoolSize = AppConfig.getInt("db.pool.max_size", 20);
        int minIdle = AppConfig.getInt("db.pool.min_idle", Math.min(5, maxPoolSize));
        long connectionTimeoutMs = AppConfig.getLong("db.pool.connection_timeout_ms", 30_000L);
        long idleTimeoutMs = AppConfig.getLong("db.pool.idle_timeout_ms", 600_000L);
        long maxLifetimeMs = AppConfig.getLong("db.pool.max_lifetime_ms", 1_800_000L);

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(jdbcUrl);
        hikari.setUsername(username);
        hikari.setPassword(password);
        hikari.setMaximumPoolSize(maxPoolSize);
        hikari.setMinimumIdle(minIdle);
        hikari.setConnectionTimeout(connectionTimeoutMs);
        hikari.setIdleTimeout(idleTimeoutMs);
        hikari.setMaxLifetime(maxLifetimeMs);
        hikari.setPoolName("pharmacy-hikari");

        return new HikariDataSource(hikari);
    }

    public static EntityManagerFactory getEntityManagerFactory() {
        return LazyHolder.FACTORY;
    }

    public static EntityManager getEntityManager() {
        return LazyHolder.FACTORY.createEntityManager();
    }

    public static void shutdown() {
        if (!initialized) {
            log.info("Hibernate is not initialized. Skip shutdown.");
            return;
        }

        if (LazyHolder.FACTORY != null && LazyHolder.FACTORY.isOpen()) {
            LazyHolder.FACTORY.close();
            log.info("Closed EntityManagerFactory.");
        }
        if (LazyHolder.DATA_SOURCE != null) {
            LazyHolder.DATA_SOURCE.close();
            log.info("Closed HikariCP datasource.");
        }
    }
}
