package com.pharmacy.app;

import com.pharmacy.mapper.DataMapper;
import com.pharmacy.network.PharmacySocketServer;
import com.pharmacy.repository.*;
import com.pharmacy.service.impl.*;
import com.pharmacy.shared.config.AppConfig;
import com.pharmacy.util.HibernateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ServerApp {

    public static void main(String[] args) {
        log.info("Starting Pharmacy Management Server.");
        long startTime = System.currentTimeMillis();

        log.info("Initializing database layer (JPA EntityManagerFactory).");
        HibernateUtil.getEntityManagerFactory();

        AccountRepository accountRepository = new AccountRepository();
        AuthorizationRepository authorizationRepository = new AuthorizationRepository();
        MedicineRepository medicineRepository = new MedicineRepository();
        DashboardRepository dashboardRepository = new DashboardRepository();
        BatchRepository batchRepository = new BatchRepository();
        SupplierRepository supplierRepository = new SupplierRepository();
        SellRepository sellRepository = new SellRepository();
        CustomerRepository customerRepository = new CustomerRepository();
        VoucherRepository voucherRepository = new VoucherRepository();
        InvoiceRepository invoiceRepository = new InvoiceRepository();
        InvoiceDetailRepository invoiceDetailRepository = new InvoiceDetailRepository();
        RefundRepository refundRepository = new RefundRepository();
        RefundDetailRepository refundDetailRepository = new RefundDetailRepository();

        Map<String, Object> serviceRegistry = new HashMap<>();
        serviceRegistry.put("MedicineService", new MedicineServiceImpl(medicineRepository, DataMapper.INSTANCE));
        serviceRegistry.put("AuthService", new AuthServiceImpl(accountRepository, authorizationRepository));
        serviceRegistry.put("DashboardService", new DashboardServiceImpl(dashboardRepository));
        serviceRegistry.put("BatchService", new BatchServiceImpl(batchRepository));
        serviceRegistry.put("SupplierService", new SupplierServiceImpl(supplierRepository));
        serviceRegistry.put("SellService", new SellServiceImpl(sellRepository, customerRepository, voucherRepository, invoiceRepository, invoiceDetailRepository, batchRepository));
        serviceRegistry.put("CustomerService", new CustomerServiceImpl(customerRepository));
        serviceRegistry.put("VoucherService", new VoucherServiceImpl(voucherRepository));
        serviceRegistry.put("RefundService", new RefundServiceImpl(refundRepository, refundDetailRepository, batchRepository));

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

        long endTime = System.currentTimeMillis() - startTime;
        log.info("Application Server successfully initialized in {} ms.", endTime);

        server.start();
    }
}
