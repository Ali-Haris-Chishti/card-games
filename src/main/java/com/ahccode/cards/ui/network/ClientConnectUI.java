package com.ahccode.cards.ui.network;

import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.context.GameContextUI;
import com.ahccode.cards.card.game.daketi.DaketiPlayer;
import com.ahccode.cards.network.GameClient;
import com.ahccode.cards.network.GameInfo;
import com.ahccode.cards.ui.StartScreen;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class ClientConnectUI extends StackPane {

    private static ClientConnectUI instance;

    private static Stage stage;
    private static Scene scene;

    // UI Components for connection state
    private Button connectButton;
    private TextField nameField;
    private TextField hostField;
    private TextField portField;
    private ProgressIndicator loadingIndicator;
    private Label statusLabel;

    // Overlay components
    private VBox mainFormContainer;
    private StackPane overlayPane;

    private ClientConnectUI() {

    }

    private ClientConnectUI(Stage stage, Scene scene) throws IOException {
        ClientConnectUI.stage = stage;
        ClientConnectUI.scene = scene;
        ClientConnectUI.stage.getIcons().add(
                new Image("file:///" + System.getProperty("user.dir") + "\\assets\\icons\\Game.png")
        );
        stage.setFullScreen(true);

        // ===== Background =====
        setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("connect-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );

        // ===== Main Content Container =====
        mainFormContainer = new VBox(15);
        mainFormContainer.setAlignment(Pos.CENTER);
        mainFormContainer.setPadding(new Insets(30));
        mainFormContainer.setMaxWidth(400);
        mainFormContainer.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.6), new CornerRadii(15), Insets.EMPTY
        )));

        // ===== Title =====
        Label title = new Label("Connect to Daketi Server");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ===== Player Name Field =====
        nameField = new TextField("");
        nameField.setPromptText("Your Name");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Host Field =====
        hostField = new TextField("");
        hostField.setPromptText("Server Host");
        hostField.setMaxWidth(Double.MAX_VALUE);
        hostField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Port Field =====
        portField = new TextField("");
        portField.setPromptText("Server Port");
        portField.setMaxWidth(Double.MAX_VALUE);
        portField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Connect Button =====
        connectButton = new Button("Connect");
        connectButton.setMaxWidth(Double.MAX_VALUE);
        connectButton.setStyle(
                "-fx-background-color: linear-gradient(#3b9d3b, #256d25);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10;"
        );
        connectButton.setOnMouseEntered(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                        "-fx-background-color: linear-gradient(#4ed14e, #2f902f);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 10;"
                );
            }
        });
        connectButton.setOnMouseExited(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                        "-fx-background-color: linear-gradient(#3b9d3b, #256d25);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 10;"
                );
            }
        });

        connectButton.setOnAction(e -> connectToServer());

        // ===== Loading Indicator =====
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);
        loadingIndicator.setPrefSize(30, 30);

        // ===== Status Label =====
        statusLabel = new Label();
        statusLabel.setTextFill(Color.WHITE);
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
        statusLabel.setVisible(false);

        // ===== Connection Status Container =====
        HBox statusContainer = new HBox(10);
        statusContainer.setAlignment(Pos.CENTER);
        statusContainer.getChildren().addAll(loadingIndicator, statusLabel);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !connectButton.isDisabled()) {
                connectToServer();
            }
        });

        // ===== Assemble Form =====
        mainFormContainer.getChildren().addAll(title, nameField, hostField, portField, connectButton, statusContainer);

        // ===== Create Overlay Pane =====
        overlayPane = new StackPane();
        overlayPane.setVisible(false);

        // ===== Add to main layout =====
        getChildren().addAll(mainFormContainer, overlayPane);

        for (Node child : mainFormContainer.getChildren()) {
            child.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER && !connectButton.isDisabled()) {
                    connectToServer();
                }
            });
        }
    }

    private void connectToServer() {
        // Validate input fields
        if (!validateInput()) {
            return;
        }

        String host = hostField.getText().trim();
        int port;
        String name = nameField.getText().trim();

        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException ex) {
            showErrorAlert("Invalid Port", "Please enter a valid numeric port number.", false);
            return;
        }

        // Validate port range
        if (port < 1 || port > 65535) {
            showErrorAlert("Invalid Port", "Port number must be between 1 and 65535.", false);
            return;
        }

        setConnectionState(true);
        updateStatus("Connecting to server...", Color.LIGHTBLUE);

        // Create connection task
        Task<GameClient> connectionTask = new Task<GameClient>() {
            @Override
            protected GameClient call() throws Exception {
                updateMessage("Connecting to " + host + ":" + port + "...");
                GameClient client = new GameClient(host, port);
                client.start();
                return client;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    try {
                        GameClient client = getValue();
                        log.info("Successfully connected to server. Creating Player for Client");

                        Player player = new DaketiPlayer(0, name, client);

                        GameContextCore.currentPlayer = player;
                        GameContextCore.gameInfo = new GameInfo(name, host, port);

                        // Unbind before setting custom message
                        statusLabel.textProperty().unbind();
                        updateStatus("Connected! Loading game...", Color.LIGHTGREEN);

                        // Add a slight delay to show success message
                        CompletableFuture.runAsync(() -> {
                            try {
                                Thread.sleep(1000);
                                Platform.runLater(() -> {
                                    scene.setRoot(StartScreen.getInstance(stage, scene));
                                    stage.setScene(scene);
                                });
                            } catch (Exception e) {
                                log.error("Error transitioning to start screen: ", e);
                                Platform.runLater(() -> {
                                    setConnectionState(false);
                                    statusLabel.textProperty().unbind();
                                    updateStatus("", Color.WHITE);
                                    showErrorAlert("Navigation Error",
                                            "Connected to server but failed to load game screen: " + e.getMessage(), true);
                                });
                            }
                        });

                    } catch (Exception e) {
                        log.error("Error creating player: ", e);
                        setConnectionState(false);
                        statusLabel.textProperty().unbind();
                        updateStatus("", Color.WHITE);
                        showErrorAlert("Connection Error",
                                "Connected to server but failed to initialize player: " + e.getMessage(), true);
                    }
                });
            }

            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    setConnectionState(false);
                    statusLabel.textProperty().unbind();
                    updateStatus("", Color.WHITE);

                    Throwable exception = getException();
                    handleConnectionError(exception, host, port);
                });
            }
        };

        // Bind status label to task message
        statusLabel.textProperty().bind(connectionTask.messageProperty());

        Thread connectionThread = new Thread(connectionTask);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }

    private boolean validateInput() {
        String name = nameField.getText().trim();
        String host = hostField.getText().trim();
        String portText = portField.getText().trim();

        if (name.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter your name.", false);
            nameField.requestFocus();
            return false;
        }

        if (host.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter the server host.", false);
            hostField.requestFocus();
            return false;
        }

        if (portText.isEmpty()) {
            showErrorAlert("Invalid Input", "Please enter the server port.", false);
            portField.requestFocus();
            return false;
        }

        if (name.length() > 20) {
            showErrorAlert("Invalid Input", "Name cannot be longer than 20 characters.", false);
            nameField.requestFocus();
            return false;
        }

        return true;
    }

    private void handleConnectionError(Throwable exception, String host, int port) {
        String title;
        String message;
        boolean showRetryDialog = true;

        if (exception instanceof ConnectException) {
            title = "Connection Refused";
            message = String.format("Could not connect to server at %s:%d.\n\n" +
                    "Possible reasons:\n" +
                    "• Server is not running\n" +
                    "• Server is not accepting connections\n" +
                    "• Wrong host or port", host, port);
        } else if (exception instanceof UnknownHostException) {
            title = "Unknown Host";
            message = String.format("Could not resolve host '%s'.\n\n" +
                    "Please check:\n" +
                    "• Host name spelling\n" +
                    "• Internet connection\n" +
                    "• DNS settings", host);
        } else if (exception instanceof SocketTimeoutException) {
            title = "Connection Timeout";
            message = String.format("Connection to %s:%d timed out.\n\n" +
                    "Possible reasons:\n" +
                    "• Server is too busy\n" +
                    "• Network connection is slow\n" +
                    "• Firewall blocking connection", host, port);
        } else if (exception instanceof IOException) {
            title = "Connection Error";
            message = String.format("Failed to connect to server at %s:%d.\n\n" +
                    "Error: %s", host, port, exception.getMessage());
        } else {
            title = "Unexpected Error";
            message = String.format("An unexpected error occurred while connecting to %s:%d.\n\n" +
                    "Error: %s", host, port, exception.getMessage());
        }

        log.error("Connection failed: ", exception);

        if (showRetryDialog) {
            showRetryDialog(title, message);
        } else {
            showErrorAlert(title, message, false);
        }
    }

    private void showRetryDialog(String title, String message) {
        showOverlay(title, message, true, () -> {
            // Retry action
            log.info("User chose to retry connection");
            hideOverlay();
        }, () -> {
            // Cancel action
            log.info("User chose to cancel connection");
            hideOverlay();
            handleUserLeave();
        });
    }

    private void handleUserLeave() {
        showOverlay("Exit Application", "Are you sure you want to exit the application?", false, () -> {
            // Exit action
            log.info("User confirmed exit");
            Platform.exit();
        }, () -> {
            // Stay action
            hideOverlay();
        });
    }

    private void setConnectionState(boolean connecting) {
        Platform.runLater(() -> {
            connectButton.setDisable(connecting);
            nameField.setDisable(connecting);
            hostField.setDisable(connecting);
            portField.setDisable(connecting);
            loadingIndicator.setVisible(connecting);
            statusLabel.setVisible(connecting || !statusLabel.getText().isEmpty());

            if (connecting) {
                connectButton.setText("Connecting...");
                connectButton.setStyle(
                        "-fx-background-color: #cccccc;" +
                                "-fx-text-fill: #666666;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 10;"
                );
            } else {
                connectButton.setText("Connect");
                connectButton.setStyle(
                        "-fx-background-color: linear-gradient(#3b9d3b, #256d25);" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-background-radius: 12;" +
                                "-fx-padding: 10;"
                );
            }
        });
    }

    private void updateStatus(String message, Color color) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            statusLabel.setTextFill(color);
            statusLabel.setVisible(!message.isEmpty());

            if (!message.isEmpty()) {
                // Add fade-in animation for status updates
                FadeTransition fade = new FadeTransition(Duration.millis(300), statusLabel);
                fade.setFromValue(0.0);
                fade.setToValue(1.0);
                fade.play();
            }
        });
    }

    public void connectToServer(String host, int port, String name) {
        // Set the fields and then connect
        Platform.runLater(() -> {
            hostField.setText(host);
            portField.setText(String.valueOf(port));
            nameField.setText(name);
            connectToServer();
        });
    }

    private void showErrorAlert(String title, String message, boolean isRetryable) {
        if (isRetryable) {
            showOverlay(title, message, true, () -> {
                // Retry action
                log.info("User chose to retry after error");
                hideOverlay();
            }, () -> {
                // OK action
                hideOverlay();
            });
        } else {
            showOverlay(title, message, false, () -> {
                // OK action
                hideOverlay();
            }, null);
        }
    }

    private void showOverlay(String title, String message, boolean isRetryable, Runnable primaryAction, Runnable secondaryAction) {
        Platform.runLater(() -> {
            // Clear any existing overlay content
            overlayPane.getChildren().clear();

            // Create semi-transparent background
            Region background = new Region();
            background.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
            background.prefWidthProperty().bind(widthProperty());
            background.prefHeightProperty().bind(heightProperty());

            // Create message container
            VBox messageContainer = new VBox(20);
            messageContainer.setAlignment(Pos.CENTER);
            messageContainer.setPadding(new Insets(40));
            messageContainer.setMaxWidth(450);
            messageContainer.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0.5, 0.0, 5.0);"
            );

            // Title label
            Label titleLabel = new Label(title);
            titleLabel.setStyle(
                    "-fx-font-size: 18px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #333333;"
            );

            // Message label
            Label messageLabel = new Label(message);
            messageLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #555555;" +
                            "-fx-wrap-text: true;"
            );
            messageLabel.setWrapText(true);
            messageLabel.setMaxWidth(350);

            // Button container
            HBox buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);

            if (isRetryable) {
                // Retry button
                Button retryButton = createOverlayButton("Try Again", "#4CAF50", "#45a049");
                retryButton.setOnAction(e -> {
                    if (primaryAction != null) primaryAction.run();
                });

                // Cancel button
                Button cancelButton = createOverlayButton("Cancel", "#f44336", "#da190b");
                cancelButton.setOnAction(e -> {
                    if (secondaryAction != null) secondaryAction.run();
                });

                buttonContainer.getChildren().addAll(retryButton, cancelButton);
            } else if (secondaryAction != null) {
                // Two button layout (like Exit/Stay)
                Button primaryButton = createOverlayButton("Exit", "#f44336", "#da190b");
                primaryButton.setOnAction(e -> {
                    if (primaryAction != null) primaryAction.run();
                });

                Button secondaryButton = createOverlayButton("Stay", "#4CAF50", "#45a049");
                secondaryButton.setOnAction(e -> {
                    if (secondaryAction != null) secondaryAction.run();
                });

                buttonContainer.getChildren().addAll(primaryButton, secondaryButton);
            } else {
                // Single OK button
                Button okButton = createOverlayButton("OK", "#2196F3", "#1976D2");
                okButton.setOnAction(e -> {
                    if (primaryAction != null) primaryAction.run();
                });

                buttonContainer.getChildren().add(okButton);
            }

            messageContainer.getChildren().addAll(titleLabel, messageLabel, buttonContainer);

            overlayPane.getChildren().addAll(background, messageContainer);

            // Dim the main form
            mainFormContainer.setEffect(new javafx.scene.effect.GaussianBlur(3));
            mainFormContainer.setOpacity(0.3);

            // Show overlay with animation
            overlayPane.setVisible(true);
            overlayPane.setOpacity(0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), overlayPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

            // Scale animation for message container
            messageContainer.setScaleX(0.8);
            messageContainer.setScaleY(0.8);

            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), messageContainer);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);
            scaleIn.play();
        });
    }

    private Button createOverlayButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 100px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);",
                baseColor
        ));

        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 100px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);",
                hoverColor
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: %s;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10 20;" +
                        "-fx-min-width: 100px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 2, 0, 0, 1);",
                baseColor
        )));

        return button;
    }

    private void hideOverlay() {
        Platform.runLater(() -> {
            if (overlayPane.isVisible()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlayPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    overlayPane.setVisible(false);
                    overlayPane.getChildren().clear();

                    // Restore main form
                    mainFormContainer.setEffect(null);
                    mainFormContainer.setOpacity(1.0);
                });
                fadeOut.play();
            }
        });
    }

    public static ClientConnectUI getInstance(Stage stage, Scene scene, boolean direct) throws IOException {
        if (instance == null) {
            instance = new ClientConnectUI(stage, scene);
            Platform.runLater(() -> {
                scene.getRoot().requestFocus(); // clears focus from all text fields
                if (direct) {
                    instance.nameField.setText(GameContextCore.gameInfo.getName());
                    instance.hostField.setText(GameContextCore.gameInfo.getHost());
                    instance.portField.setText(String.valueOf(GameContextCore.gameInfo.getPort()));
                    instance.connectButton.fire();
                }
            });

        }
        return instance;
    }

    public static ClientConnectUI getInstance() throws IOException {
        if (instance == null) {
            instance = new ClientConnectUI();
        }
        return instance;
    }

    public static void clear() {
        instance = null;
    }
}