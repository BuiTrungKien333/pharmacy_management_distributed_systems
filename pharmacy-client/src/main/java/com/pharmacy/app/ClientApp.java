package com.pharmacy.app;

import com.pharmacy.network.RpcFactory;
import com.pharmacy.network.ServerConnection;
import com.pharmacy.shared.config.AppConfig;
import com.pharmacy.shared.service.AuthService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientApp {
    public static void main(String[] args) {
        ServerConnection connection = new ServerConnection();
        try {
            log.info("Connecting to server...");
            String host = AppConfig.get("server.host");
            int port = AppConfig.getInt("server.port");

            log.info("Connecting to server -> host: {}, port: {}...", host, port);
            connection.connect(host, port);
            log.info("Connected to server successfully.");

            // 1. Initialize Factory
            RpcFactory factory = new RpcFactory(connection);

            // 2. Ask Factory to "manufacture" the AuthService
            AuthService authService = factory.createService(AuthService.class);

            // 3. Test Login feature (Replace with a real account in your DB)
            log.info("Testing login flow...");
            try {
                boolean isSuccess = authService.login("ALA010001", "123");
                log.info("Login result: {}", isSuccess ? "SUCCESS" : "FAILED");
            } catch (Exception e) {
                log.error("Login request failed: {}", e.getMessage(), e);
            }

            // Example for forgot password testing
            /*
            log.info("Testing forgot-password flow...");
            try {
                authService.forgotPassword("admin", "admin@example.com");
                log.info("OTP email was sent successfully.");
            } catch (Exception e) {
                log.error("Forgot-password request failed: {}", e.getMessage(), e);
            }
            */

        } catch (Exception e) {
            log.error("Client startup or RPC initialization failed: {}", e.getMessage(), e);
        } finally {
            connection.disconnect();
        }
    }
}
