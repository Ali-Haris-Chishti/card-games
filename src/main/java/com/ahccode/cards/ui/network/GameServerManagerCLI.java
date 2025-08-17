package com.ahccode.cards.ui.network;

import com.ahccode.cards.network.GameServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GameServerManagerCLI {

    private GameServer gameServer;
    private Thread serverThread;
    private final AtomicBoolean isServerRunning = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);
    private int port;

    public static void main(String[] args) {
        GameServerManagerCLI manager = new GameServerManagerCLI();

        // Parse command line arguments
        int port = parsePort(args);
        if (port == -1) {
            printUsage();
            System.exit(1);
        }

        manager.port = port;
        manager.run();
    }

    private static int parsePort(String[] args) {
        // Check for help flag
        for (String arg : args) {
            if (arg.equals("-h") || arg.equals("--help")) {
                return -1;
            }
        }

        // Parse port argument
        for (int i = 0; i < args.length; i++) {
            if ((args[i].equals("-p") || args[i].equals("--port")) && i + 1 < args.length) {
                try {
                    int port = Integer.parseInt(args[i + 1]);
                    if (port < 1024 || port > 65535) {
                        System.err.println("Error: Port must be between 1024 and 65535");
                        return -1;
                    }
                    return port;
                } catch (NumberFormatException e) {
                    System.err.println("Error: Invalid port number: " + args[i + 1]);
                    return -1;
                }
            }
        }

        // If no port specified, try to parse first argument as port
        if (args.length > 0 && !args[0].startsWith("-")) {
            try {
                int port = Integer.parseInt(args[0]);
                if (port < 1024 || port > 65535) {
                    System.err.println("Error: Port must be between 1024 and 65535");
                    return -1;
                }
                return port;
            } catch (NumberFormatException e) {
                System.err.println("Error: Invalid port number: " + args[0]);
                return -1;
            }
        }

        // Default port if none specified
        return 5000;
    }

    private static void printUsage() {
        System.out.println("Game Server Manager CLI");
        System.out.println("======================");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -jar game-server.jar [options] [port]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  -p, --port <port>     Server port (1024-65535, default: 5000)");
        System.out.println("  -h, --help           Show this help message");
        System.out.println();
        System.out.println("Examples:");
        System.out.println("  java -jar game-server.jar 8080");
        System.out.println("  java -jar game-server.jar --port 5000");
        System.out.println("  java -jar game-server.jar -p 3000");
        System.out.println();
        System.out.println("Interactive Commands (once running):");
        System.out.println("  start     - Start the server");
        System.out.println("  stop      - Stop the server");
        System.out.println("  restart   - Restart the server");
        System.out.println("  status    - Show server status");
        System.out.println("  help      - Show available commands");
        System.out.println("  quit/exit - Stop server and exit application");
    }

    private void run() {
        printWelcome();

        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isServerRunning.get()) {
                logMessage("INFO", "Shutting down server...");
                stopServer();
            }
        }));

        // Interactive command loop
        Scanner scanner = new Scanner(System.in);
        String command;

        logMessage("INFO", "Server ready. Type 'start' to begin or 'help' for commands.");

        while (true) {
            System.out.print("> ");
            command = scanner.nextLine().trim().toLowerCase();

            switch (command) {
                case "start":
                    startServer();
                    break;
                case "stop":
                    stopServer();
                    break;
                case "restart":
                    restartServer();
                    break;
                case "status":
                    showStatus();
                    break;
                case "help":
                    showHelp();
                    break;
                case "quit":
                case "exit":
                    if (isServerRunning.get()) {
                        logMessage("INFO", "Stopping server before exit...");
                        stopServer();
                    }
                    logMessage("INFO", "Goodbye!");
                    System.exit(0);
                    break;
                case "":
                    // Ignore empty input
                    break;
                default:
                    System.out.println("Unknown command: " + command + ". Type 'help' for available commands.");
                    break;
            }
        }
    }

    private void printWelcome() {
        System.out.println("========================================");
        System.out.println("🎮 Game Server Manager CLI");
        System.out.println("========================================");
        System.out.println("Configuration:");
        System.out.println("  Port: " + port);
        System.out.println("========================================");
    }

    private void showHelp() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  start     - Start the server on port " + port);
        System.out.println("  stop      - Stop the running server");
        System.out.println("  restart   - Restart the server");
        System.out.println("  status    - Show current server status");
        System.out.println("  help      - Show this help message");
        System.out.println("  quit/exit - Stop server and exit application");
        System.out.println();
    }

    private void showStatus() {
        System.out.println("\n--- Server Status ---");
        System.out.println("Port: " + port);
        System.out.println("Status: " + (isServerRunning.get() ? "RUNNING" : "STOPPED"));
        if (gameServer != null && isServerRunning.get()) {
            System.out.println("Connected Clients: " + getConnectedClientsCount());
        }
        System.out.println("--------------------\n");
    }

    private int getConnectedClientsCount() {
        // You'll need to implement this method in your GameServer class
        // For now, returning 0 as placeholder
        try {
            // Assuming your GameServer has a method to get client count
            return gameServer.getConnectedClientsCount();
        } catch (Exception e) {
            return 0;
        }
    }

    private void startServer() {
        if (isServerRunning.get()) {
            logMessage("WARNING", "Server is already running on port " + port);
            return;
        }

        try {
            logMessage("INFO", "Starting server on port " + port + "...");

            // Create server instance
            gameServer = new GameServer(port, this::logMessage);

            // Start server in background thread
            serverThread = new Thread(() -> {
                try {
                    isServerRunning.set(true);
                    logMessage("INFO", "✅ Server started successfully on port " + port);
                    logMessage("INFO", "Server is now accepting connections...");

                    gameServer.start(); // This blocks until server is stopped

                } catch (IOException e) {
                    if (!isShuttingDown.get()) {
                        logMessage("ERROR", "Server error: " + e.getMessage());
                        log.error("Server error: ", e);
                        resetServerState();
                    }
                }
            });

            serverThread.setDaemon(false); // Allow JVM to exit when main thread ends
            serverThread.start();

            // Wait a moment to ensure server started
            Thread.sleep(500);

        } catch (Exception e) {
            logMessage("ERROR", "Failed to start server: " + e.getMessage());
            log.error("Failed to start server: ", e);
            resetServerState();
        }
    }

    private void stopServer() {
        if (!isServerRunning.get()) {
            logMessage("WARNING", "Server is not currently running");
            return;
        }

        logMessage("INFO", "Stopping server...");

        CompletableFuture.runAsync(() -> {
            try {
                isShuttingDown.set(true);

                if (gameServer != null) {
                    gameServer.sendCloseMessage();
                    gameServer.stop();
                    gameServer.gameClosed();
                }

                if (serverThread != null && serverThread.isAlive()) {
                    serverThread.interrupt();
                    try {
                        serverThread.join(3000); // Wait up to 3 seconds
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                resetServerState();
                logMessage("INFO", "✅ Server stopped successfully");

            } catch (Exception e) {
                logMessage("ERROR", "Error stopping server: " + e.getMessage());
                log.error("Error stopping server: ", e);
            }
        }).join(); // Wait for completion
    }

    private void restartServer() {
        if (!isServerRunning.get()) {
            logMessage("INFO", "Server is not running. Starting server...");
            startServer();
            return;
        }

        logMessage("INFO", "Restarting server...");

        CompletableFuture.runAsync(() -> {
            try {
                isShuttingDown.set(true);

                if (gameServer != null) {
                    gameServer.sendCloseMessage();
                    gameServer.stop();
                    gameServer.gameClosed();
                }

                if (serverThread != null && serverThread.isAlive()) {
                    serverThread.interrupt();
                    try {
                        serverThread.join(3000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }

                // Wait a moment before restarting
                Thread.sleep(1000);

                resetServerState();

                // Start the server again
                startServer();

            } catch (Exception e) {
                logMessage("ERROR", "Error during restart: " + e.getMessage());
                log.error("Error during restart: ", e);
                resetServerState();
            }
        }).join(); // Wait for completion
    }

    private void resetServerState() {
        isServerRunning.set(false);
        isShuttingDown.set(false);
        gameServer = null;
        serverThread = null;
    }

    private void logMessage(String level, String message) {
        String timestamp = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logLine = String.format("[%s] %-7s %s", timestamp, level, message);
        System.out.println(logLine);

        // Also log to SLF4J logger for file logging if configured
        switch (level.toUpperCase()) {
            case "ERROR":
                log.error(message);
                break;
            case "WARNING":
            case "WARN":
                log.warn(message);
                break;
            case "INFO":
                log.info(message);
                break;
            case "DEBUG":
                log.debug(message);
                break;
            default:
                log.info(message);
                break;
        }
    }
}