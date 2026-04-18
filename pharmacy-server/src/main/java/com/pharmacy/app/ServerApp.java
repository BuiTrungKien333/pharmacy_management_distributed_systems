package com.pharmacy.app;

import com.pharmacy.network.PharmacySocketServer;
import com.pharmacy.repository.AccountRepository;
import com.pharmacy.repository.AuthorizationRepository;
import com.pharmacy.service.impl.AuthServiceImpl;
import com.pharmacy.service.impl.MedicineServiceImpl;
import com.pharmacy.shared.config.AppConfig;
import com.pharmacy.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServerApp {

    public static void main(String[] args) {
        log.info("Starting Pharmacy Management Server.");

        log.info("Initializing database layer (JPA EntityManagerFactory).");
        HibernateUtil.getEntityManagerFactory();

        AccountRepository accountRepository = new AccountRepository();
        AuthorizationRepository authorizationRepository = new AuthorizationRepository();

        Map<String, Object> serviceRegistry = new HashMap<>();
        serviceRegistry.put("MedicineService", new MedicineServiceImpl());
        serviceRegistry.put("AuthService", new AuthServiceImpl(accountRepository, authorizationRepository));

        log.info("Loading network configuration.");
        int port = AppConfig.getInt("server.port");
        int maxThreads = AppConfig.getInt("server.max_threads");

        PharmacySocketServer server = new PharmacySocketServer(port, maxThreads, serviceRegistry);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown signal received. Stopping server...");
            server.stop();
            HibernateUtil.shutdown();
            log.info("Shutdown completed safely.");
        }));

        server.start();
    }
}
