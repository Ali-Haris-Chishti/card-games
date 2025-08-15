package com.ahccode.cards.ui.network;

import com.ahccode.cards.network.GameServer;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class GameServerManagerUI extends Application {

    private GameServer gameServer;
    private Thread serverThread;
    private final AtomicBoolean isServerRunning = new AtomicBoolean(false);
    private final AtomicBoolean isShuttingDown = new AtomicBoolean(false);

    // UI Components
    private TextField portField;
    private Button startButton;
    private Button stopButton;
    private Button restartButton;
    private Label statusLabel;
    private TextArea logArea;
    private Label connectedClientsLabel;
    private ProgressIndicator loadingIndicator;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Game Server Manager");
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(500);
        primaryStage.getIcons().add(
                Objects.requireNonNull(new Image("file:///" + System.getProperty("user.dir") + "\\assets\\icons\\Server.png"))
        );

        // Create main layout
        VBox mainLayout = createMainLayout();

        // Apply gradient background
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.rgb(64, 81, 122, 0.9)),
                new Stop(1, Color.rgb(142, 158, 171, 0.7))
        );
        mainLayout.setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(mainLayout, 700, 600);
        primaryStage.setScene(scene);

        // Handle window close event
        primaryStage.setOnCloseRequest(e -> {
            if (isServerRunning.get()) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Server Running");
                alert.setHeaderText("Server is currently running");
                alert.setContentText("Do you want to stop the server and close the application?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    stopServer();
                    Platform.exit();
                } else {
                    e.consume();
                }
            } else {
                Platform.exit();
            }
        });

        primaryStage.show();

        // Initialize status
        updateStatus("Server Stopped", Color.RED);
        logMessageOnLogArea("INFO", "Application started. Ready to configure server.");
    }

    private VBox createMainLayout() {
        VBox layout = new VBox(20);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.TOP_CENTER);

        // Title
        Label titleLabel = new Label("🎮 Game Server Manager");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        titleLabel.setTextFill(Color.WHITE);
        DropShadow titleShadow = new DropShadow();
        titleShadow.setColor(Color.BLACK);
        titleShadow.setRadius(5);
        titleLabel.setEffect(titleShadow);

        // Server configuration section
        VBox configSection = createConfigSection();

        // Control buttons section
        HBox buttonSection = createButtonSection();

        // Status section
        VBox statusSection = createStatusSection();

        // Log section
        VBox logSection = createLogSection();

        layout.getChildren().addAll(titleLabel, configSection, buttonSection, statusSection, logSection);

        return layout;
    }

    private VBox createConfigSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);

        Label configLabel = new Label("Server Configuration");
        configLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        configLabel.setTextFill(Color.WHITE);

        HBox portBox = new HBox(10);
        portBox.setAlignment(Pos.CENTER);

        Label portLabel = new Label("Port:");
        portLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        portLabel.setTextFill(Color.WHITE);

        portField = new TextField("5000");
        portField.setPrefWidth(100);
        portField.setStyle(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-color: #cccccc; " +
                        "-fx-font-size: 14px;"
        );

        portBox.getChildren().addAll(portLabel, portField);
        section.getChildren().addAll(configLabel, portBox);

        return section;
    }

    private HBox createButtonSection() {
        HBox section = new HBox(15);
        section.setAlignment(Pos.CENTER);

        // Start Button
        startButton = createStyledButton("Start Server", "#4CAF50", "#45a049");
        startButton.setOnAction(e -> startServer());

        // Stop Button
        stopButton = createStyledButton("Stop Server", "#f44336", "#da190b");
        stopButton.setOnAction(e -> stopServer());
        stopButton.setDisable(true);

        // Restart Button
        restartButton = createStyledButton("Restart Server", "#ff9800", "#e68900");
        restartButton.setOnAction(e -> restartServer());
        restartButton.setDisable(true);

        section.getChildren().addAll(startButton, stopButton, restartButton);

        return section;
    }

    private Button createStyledButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        button.setPrefSize(130, 40);
        button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);",
                baseColor
        ));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 6, 0, 0, 3);",
                hoverColor
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: %s; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 8; " +
                        "-fx-border-radius: 8; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);",
                baseColor
        )));

        return button;
    }

    private VBox createStatusSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);

        Label statusTitleLabel = new Label("Server Status");
        statusTitleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        statusTitleLabel.setTextFill(Color.WHITE);

        statusLabel = new Label("Server Stopped");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        statusLabel.setTextFill(Color.RED);

        connectedClientsLabel = new Label("Connected Clients: 0");
        connectedClientsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        connectedClientsLabel.setTextFill(Color.WHITE);

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        section.getChildren().addAll(statusTitleLabel, statusLabel, connectedClientsLabel, loadingIndicator);

        return section;
    }

    private VBox createLogSection() {
        VBox section = new VBox(10);
        section.setAlignment(Pos.CENTER);

        Label logLabel = new Label("Server Logs");
        logLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        logLabel.setTextFill(Color.WHITE);

        logArea = new TextArea();
        logArea.setPrefRowCount(8);
        logArea.setEditable(false);
        logArea.setWrapText(true);
        logArea.setStyle(
                "-fx-background-color: #fff; " +
                        "-fx-text-fill: #000; " +
                        "-fx-font-family: 'Courier New'; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5; " +
                        "-fx-border-color: #555555;"
        );

        // Allow logArea to grow vertically and show scrollbars
        VBox.setVgrow(logArea, Priority.ALWAYS);
        logArea.setScrollTop(Double.MAX_VALUE); // Auto-scroll to bottom on new log

        Button clearLogsButton = new Button("Clear Logs");
        clearLogsButton.setOnAction(e -> logArea.clear());
        clearLogsButton.setStyle(
                "-fx-background-color: #6c757d; " +
                        "-fx-text-fill: white; " +
                        "-fx-background-radius: 5; " +
                        "-fx-border-radius: 5;"
        );

        section.getChildren().addAll(logLabel, logArea, clearLogsButton);
        VBox.setVgrow(section, Priority.ALWAYS);

        return section;
    }


    private void startServer() {
        if (isServerRunning.get()) {
            showAlert("Server Already Running", "The server is already running on port " + gameServer.getPort());
            return;
        }

        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1024 || port > 65535) {
                showAlert("Invalid Port", "Please enter a valid port number between 1024 and 65535.");
                return;
            }

            setUIState(true);
            logMessageOnLogArea("INFO", "Starting server on port " + port + "...");

            // Create server instance
            gameServer = new GameServer(port, this::logMessageOnLogArea);

            // Start server in background thread
            serverThread = new Thread(() -> {
                try {
                    isServerRunning.set(true);
                    Platform.runLater(() -> {
                        updateStatus("Server Running on Port " + port, Color.GREEN);
                        setUIState(false);
                    });

                    gameServer.start(); // This blocks until server is stopped

                } catch (IOException e) {
                    if (!isShuttingDown.get()) {
                        Platform.runLater(() -> {
                            updateStatus("Server Error", Color.RED);
                            logMessageOnLogArea("ERROR", "Server error: " + e.getMessage());
                            setUIState(false);
                            resetServerState();
                        });
                        log.error("Server error: ", e);
                    }
                }
            });

            serverThread.setDaemon(true);
            serverThread.start();

            logMessageOnLogArea("INFO", "Server started successfully on port " + port);

        } catch (NumberFormatException e) {
            showAlert("Invalid Port", "Please enter a valid numeric port number.");
            setUIState(false);
        } catch (Exception e) {
            logMessageOnLogArea("ERROR", "Failed to start server: " + e.getMessage());
            updateStatus("Failed to Start", Color.RED);
            setUIState(false);
            log.error("Failed to start server: ", e);
        }
    }

    private void stopServer() {
        if (!isServerRunning.get()) {
            showAlert("Server Not Running", "The server is not currently running.");
            return;
        }

        setUIState(true);
        logMessageOnLogArea("INFO", "Stopping server...");

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

                Platform.runLater(() -> {
                    resetServerState();
                    updateStatus("Server Stopped", Color.RED);
                    logMessageOnLogArea("INFO", "Server stopped successfully.");
                    setUIState(false);
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessageOnLogArea("ERROR", "Error stopping server: " + e.getMessage());
                    setUIState(false);
                });
                log.error("Error stopping server: ", e);
            }
        });
    }

    private void restartServer() {
        if (!isServerRunning.get()) {
            startServer();
            return;
        }

        logMessageOnLogArea("INFO", "Restarting server...");

        // Stop first, then start
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

                Platform.runLater(() -> {
                    resetServerState();
                    startServer();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    logMessageOnLogArea("INFO", "Error during restart: " + e.getMessage());
                    resetServerState();
                    setUIState(false);
                });
                log.error("Error during restart: ", e);
            }
        });
    }

    private void resetServerState() {
        isServerRunning.set(false);
        isShuttingDown.set(false);
        gameServer = null;
        serverThread = null;
        connectedClientsLabel.setText("Connected Clients: 0");
    }

    private void setUIState(boolean isLoading) {
        Platform.runLater(() -> {
            loadingIndicator.setVisible(isLoading);
            startButton.setDisable(isLoading || isServerRunning.get());
            stopButton.setDisable(isLoading || !isServerRunning.get());
            restartButton.setDisable(isLoading);
            portField.setDisable(isLoading || isServerRunning.get());
        });
    }

    private void updateStatus(String status, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(status);
            statusLabel.setTextFill(color);
        });
    }

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void logMessageOnLogArea(String logLevel, String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.appendText(String.format("[%s]  {%-10s} ---> %s%n", timestamp, logLevel, message));
        });
    }

}