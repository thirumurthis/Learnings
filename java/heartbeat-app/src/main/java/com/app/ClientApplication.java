package com.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ClientApplication {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final long PING_INTERVAL_SECONDS = 3;
    private static final Logger logger = LoggerFactory.getLogger(ClientApplication.class);

    private final String appId;
    private final int appPort;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public ClientApplication(String appId, int appPort) {
        this.appId = appId;
        this.appPort = appPort;
    }

    public void start() {
        logger.info("[{}] Starting application lifecycle...", appId);

        // Spawn background heartbeat execution loop
        scheduler.scheduleAtFixedRate(()->sendHeartbeat(appPort), 0, PING_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    private void sendHeartbeat(int appPort) {
        try (Socket socket = new Socket(SERVER_HOST, appPort);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {

            String rawPayload = HeartbeatProtocol.createMessage(appId);
            writer.println(rawPayload);
            logger.info("[{}] Dispatched health ping to central server.", appId);

        } catch (Exception e) {
            logger.error("[{}] Pipeline offline. Target monitor server unreachable.", appId);
        }
    }

    public static void main(String[] args) {
        // Provide unique app IDs when starting instances
       // String uniqueId = (args.length > 0) ? args[0] : "App-Default-Instance";

        String inputPort = null;
        String appId = null;
        int inputAppPort = 0;

        for (int i = 0; i < args.length; i++) {
            if (inputPort == null && "--port".equals(args[i]) && i + 1 < args.length) {
                inputPort = args[i + 1];
                inputAppPort = Integer.parseInt(inputPort);
                i++; // skip next element
            } else if (inputPort == null && "--port=".equals(args[i])) {
                inputPort = args[i].substring("--port=".length());
                inputAppPort = Integer.getInteger(inputPort);
            } else if (appId == null && args[i].startsWith("--appId=")) {
                appId = args[i].substring("--appId=".length());
            }else if (appId == null && args[i].startsWith("--appId")  && i + 1 < args.length){
                appId = args[i+1];
                i++; // skip next element
            }
        }

        if (inputAppPort == 0){
            inputAppPort = SERVER_PORT;
        }
        ClientApplication app = new ClientApplication(appId, inputAppPort);
        app.start();
    }
}