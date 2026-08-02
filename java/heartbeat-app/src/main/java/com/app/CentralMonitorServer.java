package com.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CentralMonitorServer {
    private static final int PORT = 8080;
    private static final long TIMEOUT_THRESHOLD_MS = 10000; // 10 seconds
    private static final Logger logger = LoggerFactory.getLogger(CentralMonitorServer.class);


    // Thread-safe map to store: Key = Application ID, Value = Last Seen Timestamp
    private static final Map<String, Long> clientRegistry = new ConcurrentHashMap<>();
    private static final ExecutorService clientHandlerPool = Executors.newCachedThreadPool();
    private static final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public static void main(String[] args) {
        logger.info("[SERVER] Initializing Central Monitor Server...");
        startTimeoutChecker();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("[SERVER] Listening for heartbeats on port " + PORT);

            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                clientHandlerPool.submit(() -> handleClientSession(clientSocket));
            }
        } catch (Exception e) {
            logger.error("[SERVER] Critical server error: {}", e.getMessage());
        } finally {
            clientHandlerPool.shutdown();
            cleanupScheduler.shutdown();
        }
    }

    private static void handleClientSession(Socket socket) {
        String clientAddress = socket.getRemoteSocketAddress().toString();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String incomingLine;
            while ((incomingLine = reader.readLine()) != null) {
                String[] parsed = HeartbeatProtocol.parseMessage(incomingLine);

                if (parsed != null && HeartbeatProtocol.PING.equals(parsed[1])) {
                    String appId = parsed[0];
                    clientRegistry.put(appId, System.currentTimeMillis());
                    logger.info("[SERVER] Health Check OK -> {} ({})", appId, clientAddress);
                }
            }
        } catch (Exception e) {
            // Connection drops are caught here
            logger.warn("[SERVER] Dropped. Exception: {}", e.getMessage());
        }
    }

    private static void startTimeoutChecker() {
        cleanupScheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            clientRegistry.forEach((appId, lastSeen) -> {
                if (now - lastSeen > TIMEOUT_THRESHOLD_MS) {
                    logger.info("[ALERT] !!! APPLICATION {} IS DEAD !!! (No ping for {} ms)",appId, (now - lastSeen));
                    clientRegistry.remove(appId);
                }
            });
        }, 0, 5, TimeUnit.SECONDS); // Check registry every 5 seconds
    }
}