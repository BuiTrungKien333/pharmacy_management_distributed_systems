package com.pharmacy.network;

import com.pharmacy.shared.config.AppConfig;
import com.pharmacy.shared.dto.response.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.*;

@Slf4j
public class PharmacySocketServer {

    private final int port;
    private final ExecutorService executor;
    private final Semaphore sessionPermits;
    private final int maxConcurrentSessions;
    private ServerSocket serverSocket;
    private volatile boolean isRunning = false;
    private final Map<String, Object> serviceRegistry;

    public PharmacySocketServer(int port, int maxThreads, Map<String, Object> serviceRegistry) {
        this.port = port;
        this.serviceRegistry = serviceRegistry;

        boolean useVirtualThreads = AppConfig.getBoolean("server.use_virtual_threads", true);
        this.maxConcurrentSessions = AppConfig.getInt("server.max_concurrent_sessions", 500);
        int queueSize = AppConfig.getInt("server.threadpool.queue_size", 1000);

        this.sessionPermits = new Semaphore(this.maxConcurrentSessions);

        if (useVirtualThreads) {
            this.executor = Executors.newVirtualThreadPerTaskExecutor();
            log.info("Using virtual threads for client sessions (maxConcurrentSessions={}).", this.maxConcurrentSessions);
        } else {
            this.executor = new ThreadPoolExecutor(
                    maxThreads,
                    maxThreads,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(queueSize),
                    new ThreadPoolExecutor.AbortPolicy()
            );
            log.info("Using bounded fixed thread pool for client sessions (threads={}, queueSize={}, maxConcurrentSessions={}).",
                    maxThreads, queueSize, this.maxConcurrentSessions);
        }
    }

    public void start() {
        try {
            serverSocket = new ServerSocket();
            serverSocket.setReuseAddress(true);
            serverSocket.bind(new InetSocketAddress(port), 100);
            isRunning = true;
            log.info("event=socket_server_started port={}", port);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                String clientIp = clientSocket.getInetAddress().getHostAddress();
                int clientPort = clientSocket.getPort();

                if (!sessionPermits.tryAcquire()) {
                    log.warn("event=connection_rejected clientIp={} clientPort={} reason=server_busy", clientIp, clientPort);
                    sendServerBusyAndClose(clientSocket);
                    continue;
                }

                applySocketTuning(clientSocket);
                log.info("event=client_connection_accepted clientIp={} clientPort={} activeSessions={}.",
                        clientIp, clientPort, getActiveSessions());

                try {
                    executor.submit(() -> {
                        try {
                            new ClientHandler(clientSocket, serviceRegistry).run();
                        } finally {
                            sessionPermits.release();
                        }
                    });
                } catch (RejectedExecutionException e) {
                    sessionPermits.release();
                    log.error("Server overloaded, rejected connection from {}", clientIp);
                    sendServerBusyAndClose(clientSocket);
                }
            }
        } catch (IOException e) {
            if (isRunning) {
                log.error("Network error while running the socket server.", e);
            } else {
                log.error("Failed to start socket server on port {}.", port, e);
            }
        }
    }

    public void stop() {
        log.info("Stopping socket server...");
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executor != null) {
                executor.shutdown();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    log.warn("Force shutting down worker threads...");
                    executor.shutdownNow();
                }
            }
            log.info("Socket server stopped.");
        } catch (Exception e) {
            log.error("Failed to stop socket server cleanly.", e);
        }
    }

    private void applySocketTuning(Socket socket) {
        try {
            int readTimeoutMs = AppConfig.getInt("server.socket.read_timeout_ms", 30_000);
            boolean keepAlive = AppConfig.getBoolean("server.socket.keep_alive", true);
            boolean tcpNoDelay = AppConfig.getBoolean("server.socket.tcp_no_delay", true);

            socket.setSoTimeout(readTimeoutMs);
            socket.setKeepAlive(keepAlive);
            socket.setTcpNoDelay(tcpNoDelay);
        } catch (Exception e) {
            log.debug("Failed to apply socket tuning.", e);
        }
    }

    private void sendServerBusyAndClose(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());

            RpcResponse busyResponse = new RpcResponse();
            busyResponse.setException(new RuntimeException("Server is busy (HTTP 503). Please retry later."));

            out.writeObject(busyResponse);
            out.flush();
        } catch (Exception ignored) {
            // best-effort
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private int getActiveSessions() {
        return Math.max(0, this.maxConcurrentSessions - sessionPermits.availablePermits());
    }
}