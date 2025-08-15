package com.ahccode.cards;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.ui.StartScreen;
import com.ahccode.cards.ui.daketi.DaketiGameFinishedScreen;
import com.ahccode.cards.ui.network.ClientConnectUI;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.control.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static com.ahccode.cards.ui.daketi.DaketiGameFinishedScreen.GameResult.*;

@Slf4j
public class ClientMainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static Scene scene;
    @Getter
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        stage.setFullScreen(true);
        startGameInitials(stage);
        stage.show();
//        testEndScreen(stage);
    }

    public static void startGameInitials(Stage stage) throws IOException {
        GameContextCore.GAME_FINISHED = false;
        scene = new Scene(new Pane());
        scene.setRoot(ClientConnectUI.getInstance(stage, scene));
        stage.setTitle("Cards");
        stage.setScene(scene);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.setFullScreen(true);

        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                try {
                    if ( !(scene.getRoot() instanceof ClientConnectUI))
                        GameContextCore.currentPlayer.getAssociatedClient().send(new Message(MessageType.PLAYER_LEFT, null));
                    System.exit(0);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public static void restartGame() {
        restartGame(true); // Default to disconnect behavior for backward compatibility
    }

    public static void restartGame(boolean isDisconnected) {
        log.info("Restarting Game: Disconnected? {}, GAME_FINISHED? {}", isDisconnected, GameContextCore.GAME_FINISHED);
        if (isDisconnected) {
            showDisconnectNotification(() -> {
                try {
                    clearAll();
                    startGameInitials(primaryStage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } else {
            showPlayAgainTransition(() -> {
                try {
                    clearAll();
                    startGameInitials(primaryStage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static void showPlayAgainTransition(Runnable onComplete) {
        Platform.runLater(() -> {
            // Create transition overlay
            StackPane transitionOverlay = new StackPane();
            transitionOverlay.setStyle("-fx-background-color: rgba(26, 35, 46, 0.95);"); // Dark blue overlay

            // Create content container
            VBox contentContainer = new VBox(25);
            contentContainer.setAlignment(Pos.CENTER);
            contentContainer.setPadding(new Insets(40));
            contentContainer.setMaxWidth(450);
            contentContainer.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #34495e, #2c3e50);" +
                            "-fx-background-radius: 15;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 12, 0.6, 0.0, 6.0);" +
                            "-fx-border-color: linear-gradient(to bottom, #3498db, #2980b9);" +
                            "-fx-border-width: 1.5;" +
                            "-fx-border-radius: 15;"
            );

            // Game transition icon (cards shuffling symbol)
            Label gameIcon = new Label("🃏");
            gameIcon.setStyle(
                    "-fx-font-size: 42px;" +
                            "-fx-text-fill: #3498db;"
            );

            // Rotating animation for the icon
            RotateTransition iconRotate = new RotateTransition(Duration.millis(2000), gameIcon);
            iconRotate.setByAngle(360);
            iconRotate.setCycleCount(Timeline.INDEFINITE);
            iconRotate.setInterpolator(Interpolator.LINEAR);
            iconRotate.play();

            // Title
            Label titleLabel = new Label("New Game Starting");
            titleLabel.setStyle(
                    "-fx-font-size: 24px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #ecf0f1;"
            );

            // Subtitle message
            Label subtitleLabel = new Label("Preparing for another round...");
            subtitleLabel.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-text-fill: #bdc3c7;" +
                            "-fx-font-style: italic;" +
                            "-fx-text-alignment: center;"
            );

            // Loading dots animation
            HBox loadingDots = new HBox(8);
            loadingDots.setAlignment(Pos.CENTER);

            // Create 3 animated dots
            for (int i = 0; i < 3; i++) {
                Label dot = new Label("●");
                dot.setStyle(
                        "-fx-font-size: 20px;" +
                                "-fx-text-fill: #3498db;"
                );

                // Create bounce animation for each dot with delay
                ScaleTransition bounce = new ScaleTransition(Duration.millis(600), dot);
                bounce.setFromX(1.0);
                bounce.setFromY(1.0);
                bounce.setToX(1.4);
                bounce.setToY(1.4);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(Timeline.INDEFINITE);
                bounce.setDelay(Duration.millis(i * 200));
                bounce.play();

                loadingDots.getChildren().add(dot);
            }

            // Progress bar with smooth animation
            ProgressBar progressBar = new ProgressBar(0.0);
            progressBar.setPrefWidth(300);
            progressBar.setPrefHeight(8);
            progressBar.setStyle(
                    "-fx-accent: linear-gradient(to right, #3498db, #2ecc71);" +
                            "-fx-control-inner-background: #34495e;" +
                            "-fx-background-radius: 4;" +
                            "-fx-border-radius: 4;"
            );

            // Animate progress bar
            Timeline progressAnimation = new Timeline();
            KeyValue progressKeyValue = new KeyValue(progressBar.progressProperty(), 1.0, Interpolator.EASE_BOTH);
            KeyFrame progressKeyFrame = new KeyFrame(Duration.millis(2500), progressKeyValue);
            progressAnimation.getKeyFrames().add(progressKeyFrame);
            progressAnimation.play();

            // Status message
            Label statusLabel = new Label("Setting up connection...");
            statusLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #95a5a6;"
            );

            // Animate status text changes
            Timeline statusAnimation = new Timeline(
                    new KeyFrame(Duration.millis(0), e -> statusLabel.setText("Setting up connection...")),
                    new KeyFrame(Duration.millis(800), e -> statusLabel.setText("Clearing game state...")),
                    new KeyFrame(Duration.millis(1600), e -> statusLabel.setText("Loading connection screen...")),
                    new KeyFrame(Duration.millis(2400), e -> statusLabel.setText("Almost ready!"))
            );
            statusAnimation.play();

            // Add all elements to content container
            contentContainer.getChildren().addAll(
                    gameIcon,
                    titleLabel,
                    subtitleLabel,
                    loadingDots,
                    progressBar,
                    statusLabel
            );

            transitionOverlay.getChildren().add(contentContainer);

            // Get current scene and add overlay
            Scene currentScene = primaryStage.getScene();
            if (currentScene != null && currentScene.getRoot() instanceof Parent) {
                Parent currentRoot = (Parent) currentScene.getRoot();

                // If current root is a StackPane, add overlay to it
                if (currentRoot instanceof StackPane) {
                    ((StackPane) currentRoot).getChildren().add(transitionOverlay);
                } else {
                    // Otherwise, wrap current root in a new StackPane
                    StackPane wrapper = new StackPane();
                    wrapper.getChildren().addAll(currentRoot, transitionOverlay);
                    currentScene.setRoot(wrapper);
                }
            } else {
                // Fallback: create new scene with just the overlay
                Scene overlayScene = new Scene(transitionOverlay);
                primaryStage.setScene(overlayScene);
            }

            // Add entrance animation
            transitionOverlay.setOpacity(0);
            contentContainer.setScaleX(0.8);
            contentContainer.setScaleY(0.8);

            // Fade in overlay with slide up effect
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), transitionOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Scale and slide in content
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(500), contentContainer);
            scaleIn.setFromX(0.8);
            scaleIn.setFromY(0.8);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            TranslateTransition slideIn = new TranslateTransition(Duration.millis(500), contentContainer);
            slideIn.setFromY(30);
            slideIn.setToY(0);

            // Play entrance animations
            ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn, slideIn);
            entrance.setInterpolator(Interpolator.EASE_OUT);
            entrance.play();

            // Auto-proceed after animations complete
            Timeline autoProceed = new Timeline(new KeyFrame(Duration.millis(3000), e -> {
                // Fade out animation before proceeding
                FadeTransition fadeOut = new FadeTransition(Duration.millis(400), transitionOverlay);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
                fadeOut.play();
            }));
            autoProceed.play();

            // Add click to skip functionality
            transitionOverlay.setOnMouseClicked(e -> {
                autoProceed.stop();
                progressAnimation.stop();
                statusAnimation.stop();
                iconRotate.stop();

                FadeTransition quickFadeOut = new FadeTransition(Duration.millis(200), transitionOverlay);
                quickFadeOut.setFromValue(1.0);
                quickFadeOut.setToValue(0.0);
                quickFadeOut.setOnFinished(event -> {
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
                quickFadeOut.play();
            });

            // Keyboard shortcut to skip
            transitionOverlay.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.SPACE || e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.ESCAPE) {
                    autoProceed.stop();
                    progressAnimation.stop();
                    statusAnimation.stop();
                    iconRotate.stop();

                    FadeTransition quickFadeOut = new FadeTransition(Duration.millis(200), transitionOverlay);
                    quickFadeOut.setFromValue(1.0);
                    quickFadeOut.setToValue(0.0);
                    quickFadeOut.setOnFinished(event -> {
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    });
                    quickFadeOut.play();
                }
            });

            // Make overlay focusable for keyboard events
            transitionOverlay.setFocusTraversable(true);
            transitionOverlay.requestFocus();
        });
    }

    public static void showDisconnectNotification(Runnable onComplete) {
        Platform.runLater(() -> {
            // Create disconnect overlay
            StackPane disconnectOverlay = new StackPane();
            disconnectOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

            // Create content container
            VBox contentContainer = new VBox(30);
            contentContainer.setAlignment(Pos.CENTER);
            contentContainer.setPadding(new Insets(50));
            contentContainer.setMaxWidth(500);
            contentContainer.setStyle(
                    "-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e);" +
                            "-fx-background-radius: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 15, 0.5, 0.0, 8.0);" +
                            "-fx-border-color: #3498db;" +
                            "-fx-border-width: 2;" +
                            "-fx-border-radius: 20;"
            );

            // Disconnect icon (using Unicode symbol)
            Label disconnectIcon = new Label("⚠");
            disconnectIcon.setStyle(
                    "-fx-font-size: 48px;" +
                            "-fx-text-fill: #e74c3c;" +
                            "-fx-font-weight: bold;"
            );

            // Title
            Label titleLabel = new Label("Connection Lost");
            titleLabel.setStyle(
                    "-fx-font-size: 28px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-text-fill: #ecf0f1;"
            );

            // Message
            Label messageLabel = new Label("You have been disconnected from the server.\nThe server may have shut down or there was a network issue.");
            messageLabel.setStyle(
                    "-fx-font-size: 16px;" +
                            "-fx-text-fill: #bdc3c7;" +
                            "-fx-text-alignment: center;" +
                            "-fx-wrap-text: true;"
            );
            messageLabel.setWrapText(true);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setMaxWidth(400);

            // Progress indicator with custom styling
            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(40, 40);
            progressIndicator.setStyle(
                    "-fx-progress-color: #3498db;" +
                            "-fx-accent: #3498db;"
            );

            // Auto-reconnect message
            Label autoReconnectLabel = new Label("Returning to connection screen...");
            autoReconnectLabel.setStyle(
                    "-fx-font-size: 14px;" +
                            "-fx-text-fill: #95a5a6;" +
                            "-fx-font-style: italic;"
            );

            // Button container
            HBox buttonContainer = new HBox(15);
            buttonContainer.setAlignment(Pos.CENTER);

            // Reconnect button
            Button reconnectButton = createStyledButton("Reconnect Now", "#27ae60", "#2ecc71");
            reconnectButton.setOnAction(e -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            });

            // Exit button
            Button exitButton = createStyledButton("Exit Game", "#c0392b", "#e74c3c");
            exitButton.setOnAction(e -> {
                Platform.exit();
                System.exit(0);
            });

            buttonContainer.getChildren().addAll(reconnectButton, exitButton);

            // Progress container
            VBox progressContainer = new VBox(10);
            progressContainer.setAlignment(Pos.CENTER);
            progressContainer.getChildren().addAll(progressIndicator, autoReconnectLabel);

            // Add all elements to content container
            contentContainer.getChildren().addAll(
                    disconnectIcon,
                    titleLabel,
                    messageLabel,
                    progressContainer,
                    buttonContainer
            );

            disconnectOverlay.getChildren().add(contentContainer);

            // Get current scene and add overlay
            Scene currentScene = primaryStage.getScene();
            if (currentScene != null && currentScene.getRoot() instanceof Parent) {
                Parent currentRoot = (Parent) currentScene.getRoot();

                // If current root is a StackPane, add overlay to it
                if (currentRoot instanceof StackPane) {
                    ((StackPane) currentRoot).getChildren().add(disconnectOverlay);
                } else {
                    // Otherwise, wrap current root in a new StackPane
                    StackPane wrapper = new StackPane();
                    wrapper.getChildren().addAll(currentRoot, disconnectOverlay);
                    currentScene.setRoot(wrapper);
                }
            } else {
                // Fallback: create new scene with just the overlay
                Scene overlayScene = new Scene(disconnectOverlay);
                primaryStage.setScene(overlayScene);
            }

            // Add entrance animation
            disconnectOverlay.setOpacity(0);
            contentContainer.setScaleX(0.7);
            contentContainer.setScaleY(0.7);

            // Fade in overlay
            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), disconnectOverlay);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            // Scale in content
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(400), contentContainer);
            scaleIn.setFromX(0.7);
            scaleIn.setFromY(0.7);
            scaleIn.setToX(1.0);
            scaleIn.setToY(1.0);

            // Play animations
            ParallelTransition entrance = new ParallelTransition(fadeIn, scaleIn);
            entrance.setInterpolator(Interpolator.EASE_OUT);
            entrance.play();

            // Auto-redirect after 5 seconds (optional)
            Timeline autoRedirect = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                if (onComplete != null) {
                    onComplete.run();
                }
            }));
            autoRedirect.play();

            // Add keyboard shortcuts
            disconnectOverlay.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER || e.getCode() == KeyCode.SPACE) {
                    autoRedirect.stop();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } else if (e.getCode() == KeyCode.ESCAPE) {
                    Platform.exit();
                    System.exit(0);
                }
            });

            // Focus the overlay to receive key events
            disconnectOverlay.requestFocus();
            disconnectOverlay.setFocusTraversable(true);
        });
    }

    private static Button createStyledButton(String text, String baseColor, String hoverColor) {
        Button button = new Button(text);
        button.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 24;" +
                        "-fx-min-width: 140px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);" +
                        "-fx-cursor: hand;",
                baseColor, darkenColor(baseColor)
        ));

        // Hover effect
        button.setOnMouseEntered(e -> button.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 24;" +
                        "-fx-min-width: 140px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 6, 0, 0, 3);" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.05;" +
                        "-fx-scale-y: 1.05;",
                hoverColor, darkenColor(hoverColor)
        )));

        button.setOnMouseExited(e -> button.setStyle(String.format(
                "-fx-background-color: linear-gradient(to bottom, %s, %s);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12 24;" +
                        "-fx-min-width: 140px;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 4, 0, 0, 2);" +
                        "-fx-cursor: hand;" +
                        "-fx-scale-x: 1.0;" +
                        "-fx-scale-y: 1.0;",
                baseColor, darkenColor(baseColor)
        )));

        return button;
    }

    private static String darkenColor(String hexColor) {
        // Simple color darkening - in a real implementation you might want more sophisticated color manipulation
        if (hexColor.equals("#27ae60")) return "#1e8449";
        if (hexColor.equals("#2ecc71")) return "#27ae60";
        if (hexColor.equals("#c0392b")) return "#922b21";
        if (hexColor.equals("#e74c3c")) return "#c0392b";
        return hexColor; // fallback
    }

    public static void clearAll() throws IOException {
        if (GameContextCore.currentPlayer != null)
            GameContextCore.currentPlayer.clear();
        GameContextCore.currentPlayer = null;
        GameContextCore.turn = 0;
        GameContextCore.deck = null;
        ClientConnectUI.clear();
        StartScreen.clear();
    }

    public void testEndScreen(Stage stage) {
        List<Card> stackA = Arrays.asList(
                new Card(CardFamily.CLUB, CardNumber.EIGHT, true, true),
                new Card(CardFamily.CLUB, CardNumber.ACE, true, true),
                new Card(CardFamily.SPADE, CardNumber.EIGHT, true, true)
        );
        List<Card> stackB = Arrays.asList(
                new Card(CardFamily.CLUB, CardNumber.KING, true, true),
                new Card(CardFamily.CLUB, CardNumber.TEN, true, true),
                new Card(CardFamily.SPADE, CardNumber.EIGHT, true, true),
                new Card(CardFamily.HEART, CardNumber.KING, true, true),
                new Card(CardFamily.DIAMOND, CardNumber.TEN, true, true),
                new Card(CardFamily.SPADE, CardNumber.TWO, true, true)
        );

        int scoreA = stackA.stream().mapToInt(card -> card.getCardNumber().getDakettiScore()).sum();
        int scoreB = stackB.stream().mapToInt(card -> card.getCardNumber().getDakettiScore()).sum();

        Scene scene = new Scene(new DaketiGameFinishedScreen(stackA, stackB, scoreA, scoreB, WIN));
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                System.exit(0);
            }
        });
    }
}