package com.pharmacy.app;

import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.fonts.roboto.FlatRobotoFont;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.pharmacy.network.RpcFactory;
import com.pharmacy.network.ServerConnection;
import com.pharmacy.shared.config.AppConfig;
import com.pharmacy.util.ClientContext;
import com.pharmacy.view.MainUI;
import com.pharmacy.view.auth.LoginUI;
import com.pharmacy.view.splashscreen.SplashScreenUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class ClientApp extends JFrame {

    private static ClientApp app;

    private final LoginUI loginUI;
    private final MainUI mainUI;

    public ClientApp() {
        initComponents();

        mainUI = new MainUI();
        loginUI = new LoginUI();

        loginUI.setVisible(true);
        setContentPane(loginUI);
        getRootPane().putClientProperty("JRootPane.useWindowDecorations", true);
    }

    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new GroupLayout(getContentPane()));
        setSize(1000, 700);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public static void showForm(Component component) {
        log.debug("Navigating to form: {}", component.getClass().getSimpleName());
        component.applyComponentOrientation(app.getComponentOrientation());
        app.mainUI.showForm(component);
    }

    public static void loginSuccess() {
        log.info("Login successful. Initializing Main Dashboard.");

        // FIX 3: Changed app.login to app.loginUI
        app.loginUI.setVisible(false);
        FlatAnimatedLafChange.showSnapshot();
        app.setContentPane(app.mainUI);
        app.mainUI.applyComponentOrientation(app.getComponentOrientation());
        setSelectedMenu(0, 0);
        app.mainUI.hideMenu();
        SwingUtilities.updateComponentTreeUI(app.mainUI);

        FlatAnimatedLafChange.hideSnapshotWithAnimation();
        SwingUtilities.invokeLater(() -> {
            app.setResizable(true);
            app.setExtendedState(JFrame.MAXIMIZED_BOTH);
        });
    }

    public static void logoutSuccess() {
        log.info("User logged out. Cleaning up and returning to Login.");

        app.setSize(1000, 700);
        app.setLocationRelativeTo(null);
        app.setResizable(false);

        app.loginUI.setVisible(true);
        app.loginUI.resetLogin();

        FlatAnimatedLafChange.showSnapshot();
        app.setContentPane(app.loginUI);
        app.loginUI.applyComponentOrientation(app.getComponentOrientation());
        SwingUtilities.updateComponentTreeUI(app.loginUI);
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    public static void setSelectedMenu(int index, int subIndex) {
        app.mainUI.setSelectedMenu(index, subIndex);
    }

    public static ClientApp getInstance() {
        return app;
    }

    public static void main(String[] args) {
        SplashScreenUI splash = new SplashScreenUI(null, false);
        splash.setVisible(true);

        new Thread(() -> {
            log.info("Start initializing AlamiPharmacy Client App...");
            long startTime = System.currentTimeMillis();

            ServerConnection connection = new ServerConnection();
            ClientApp[] appHolder = new ClientApp[1];

            try {
                splash.updateProgress("Loading Themes...", 10);
                try {
                    FlatRobotoFont.install();
                    FlatLaf.registerCustomDefaultsSource("themes");
                    UIManager.put("defaultFont", new Font(FlatRobotoFont.FAMILY, Font.PLAIN, 13));
                    FlatMacLightLaf.setup();
                    log.info("Theme loaded successfully.");
                } catch (Exception e) {
                    log.error("Failed to initialize theme", e);
                }

                splash.updateProgress("Connecting to Server...", 40);
                String host = AppConfig.get("server.host");
                int port = AppConfig.getInt("server.port");
                log.info("Connecting to server -> host: {}, port: {}...", host, port);

                connection.connect(host, port);
                log.info("Connected to server successfully.");

                RpcFactory rpcFactory = new RpcFactory(connection);

                ClientContext.init(rpcFactory);

                splash.updateProgress("Initializing Modules...", 80);
                SwingUtilities.invokeAndWait(() -> {
                    appHolder[0] = new ClientApp();
                    ClientApp.app = appHolder[0];
                });

                splash.updateProgress("Starting Application...", 100);
                splash.dispose();

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutting down Client, disconnecting from Server...");
                    connection.disconnect();
                }));

                EventQueue.invokeLater(() -> {
                    if (appHolder[0] != null) {
                        appHolder[0].setVisible(true);
                    }
                });

                long endTime = System.currentTimeMillis() - startTime;
                log.info("Application successfully initialized in {} ms.", endTime);

            } catch (Exception e) {
                log.error("Application failed to start", e);
                splash.dispose();
                connection.disconnect();

                JOptionPane.showMessageDialog(null,
                        "Lỗi khởi động ứng dụng (Không thể kết nối đến Máy chủ):\n" + e.getMessage(),
                        "Lỗi Mạng", JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            }
        }).start();
    }
}