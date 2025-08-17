package com.ahccode.client.gui;

import com.ahccode.client.context.ClientContext;
import com.ahccode.client.game.Team;
import com.ahccode.client.gui.component.NameBox;
import com.ahccode.common.network.PlayerInfo;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;


import java.util.*;

public class PlayerWaitingScreen extends Pane {

    List<NameBox> nameBoxes;
    private Label centerLabel;
    private Timer countdownTimer;
    private TimerTask countdownTask;

    // Animation elements
    private Circle pulsingCircle;
    private VBox animationContainer;
    private Timeline pulseAnimation;
    private Timeline fadeAnimation;
    private Timeline scaleAnimation;
    private RotateTransition rotateTransition;

    private static PlayerWaitingScreen instance;

    private PlayerWaitingScreen() {
        initializeCenterLabel();
        initializeAnimationElements();
    }

    private void initializeCenterLabel() {
        centerLabel = new Label("Waiting for Players...");
        centerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        centerLabel.setTextFill(Color.DARKBLUE);
        centerLabel.setTextAlignment(TextAlignment.CENTER);
        centerLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                "-fx-background-radius: 20; " +
                "-fx-padding: 25; " +
                "-fx-border-color: #2E86AB; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 20; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 2);");

        // Create animation container
        animationContainer = new VBox();
        animationContainer.setSpacing(20);
        animationContainer.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().add(animationContainer);
    }

    private void initializeAnimationElements() {
        // Create pulsing circle for waiting animation
        pulsingCircle = new Circle(8);
        pulsingCircle.setFill(Color.TRANSPARENT);
        pulsingCircle.setStroke(Color.DODGERBLUE);
        pulsingCircle.setStrokeWidth(3);
        pulsingCircle.setOpacity(0.7);

        // Pulsing animation for waiting state
        pulseAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(pulsingCircle.radiusProperty(), 8),
                        new KeyValue(pulsingCircle.opacityProperty(), 0.7)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(pulsingCircle.radiusProperty(), 25),
                        new KeyValue(pulsingCircle.opacityProperty(), 0.1))
        );
        pulseAnimation.setCycleCount(Timeline.INDEFINITE);
        pulseAnimation.setAutoReverse(true);

        animationContainer.getChildren().addAll(centerLabel, pulsingCircle);
    }

    public void initialize() {
        ClientContext.currentPlayer.sendHelloToServer();
        updatePlayerInfoList(new ArrayList<>());
        startWaitingAnimation();
    }

    private void startWaitingAnimation() {
        pulsingCircle.setVisible(true);
        pulseAnimation.play();

        // Gentle floating animation for the center label
        Timeline floatAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(centerLabel.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(2), new KeyValue(centerLabel.translateYProperty(), -8)),
                new KeyFrame(Duration.seconds(4), new KeyValue(centerLabel.translateYProperty(), 0))
        );
        floatAnimation.setCycleCount(Timeline.INDEFINITE);
        floatAnimation.play();
    }

    private void stopWaitingAnimation() {
        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }
        pulsingCircle.setVisible(false);
    }

    @Override
    protected void layoutChildren() {
        double padding = 20;

        double width = getWidth();
        double height = getHeight();

        // Position animation container in center
        double containerWidth = animationContainer.prefWidth(-1);
        double containerHeight = animationContainer.prefHeight(-1);
        animationContainer.resizeRelocate(
                (width - containerWidth) / 2,
                (height - containerHeight) / 2,
                containerWidth,
                containerHeight
        );

        // Position name boxes (4 players) around the edges
        if (nameBoxes != null && nameBoxes.size() >= 4) {
            NameBox topLeftBox = nameBoxes.get(0);
            NameBox topRightBox = nameBoxes.get(1);
            NameBox bottomRightBox = nameBoxes.get(2);
            NameBox bottomLeftBox = nameBoxes.get(3);

            double topLeftWidth = topLeftBox.prefWidth(-1);
            double topLeftHeight = topLeftBox.prefHeight(-1);
            double topRightWidth = topRightBox.prefWidth(-1);
            double topRightHeight = topRightBox.prefHeight(-1);
            double bottomRightWidth = bottomRightBox.prefWidth(-1);
            double bottomRightHeight = bottomRightBox.prefHeight(-1);
            double bottomLeftWidth = bottomLeftBox.prefWidth(-1);
            double bottomLeftHeight = bottomLeftBox.prefHeight(-1);

            // Position with smooth entrance animations
            topLeftBox.resizeRelocate(padding, padding, topLeftWidth, topLeftHeight);
            topRightBox.resizeRelocate(width - topRightWidth - padding, padding, topRightWidth, topRightHeight);
            bottomRightBox.resizeRelocate(width - bottomRightWidth - padding, height - bottomRightHeight - padding, bottomRightWidth, bottomRightHeight);
            bottomLeftBox.resizeRelocate(padding, height - bottomLeftHeight - padding, bottomLeftWidth, bottomLeftHeight);
        }
    }

    public static PlayerWaitingScreen getInstance() {
        if (instance == null) {
            instance = new PlayerWaitingScreen();
        }
        return instance;
    }

    public void playerConnected(PlayerInfo info) {
        NameBox newNameBox = new NameBox(info.getPlayerNumber() % 2 == 0? Team.TEAM_GREEN: Team.TEAM_BLUE, info.getName());
        nameBoxes.add(newNameBox);

        // Animate new player entrance
        newNameBox.setOpacity(0);
        newNameBox.setScaleX(0.5);
        newNameBox.setScaleY(0.5);

        Timeline entranceAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(newNameBox.opacityProperty(), 0),
                        new KeyValue(newNameBox.scaleXProperty(), 0.5),
                        new KeyValue(newNameBox.scaleYProperty(), 0.5)),
                new KeyFrame(Duration.seconds(0.5),
                        new KeyValue(newNameBox.opacityProperty(), 1),
                        new KeyValue(newNameBox.scaleXProperty(), 1),
                        new KeyValue(newNameBox.scaleYProperty(), 1))
        );
        entranceAnimation.play();
    }

    private int previousPlayerCount = 0;

    public void updatePlayerInfoList(List<PlayerInfo> playerInfos) {
        boolean isNewPlayerAdded = playerInfos.size() > previousPlayerCount;

        // Clear previous nameboxes but keep the animation container
        getChildren().removeIf(child -> child instanceof NameBox);

        nameBoxes = new ArrayList<>(4);

        // Create all name boxes first
        for (int i = 0; i < playerInfos.size(); i++) {
            NameBox nameBox = new NameBox(i % 2 == 0? Team.TEAM_GREEN: Team.TEAM_BLUE, playerInfos.get(i).getName());
            nameBoxes.add(nameBox);
            getChildren().add(nameBox);
        }

        // Add empty boxes for remaining slots
        for (int i = playerInfos.size(); i < 4; i++) {
            NameBox emptyBox = new NameBox();
            nameBoxes.add(emptyBox);
            getChildren().add(emptyBox);
            emptyBox.setOpacity(1);
        }

        if (isNewPlayerAdded && previousPlayerCount > 0) {
            // NEW PLAYER CELEBRATION ANIMATION!
            showNewPlayerCelebration(playerInfos);
        } else {
            // Initial load - show all existing players with staggered entrance
            showInitialPlayerLoad(playerInfos);
        }

        previousPlayerCount = playerInfos.size();
    }

    private void showNewPlayerCelebration(List<PlayerInfo> playerInfos) {
        int newPlayerIndex = playerInfos.size() - 1;
        NameBox newPlayerBox = nameBoxes.get(newPlayerIndex);

        // 1. Create celebration effect around the new player
        Circle celebrationRing = new Circle(0);
        celebrationRing.setFill(Color.TRANSPARENT);
        celebrationRing.setStroke(Color.GOLD);
        celebrationRing.setStrokeWidth(4);
        celebrationRing.setOpacity(0.8);
        getChildren().add(celebrationRing);

        // Position the ring at the new player's location (we'll update this in layout)
        celebrationRing.centerXProperty().bind(newPlayerBox.layoutXProperty().add(newPlayerBox.widthProperty().divide(2)));
        celebrationRing.centerYProperty().bind(newPlayerBox.layoutYProperty().add(newPlayerBox.heightProperty().divide(2)));

        // 2. Animate existing players first with subtle acknowledgment
        for (int i = 0; i < newPlayerIndex; i++) {
            NameBox existingBox = nameBoxes.get(i);
            existingBox.setOpacity(1);

            // Gentle "welcome" nod animation for existing players
            Timeline welcomeNod = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(existingBox.scaleYProperty(), 1)),
                    new KeyFrame(Duration.seconds(0.2), new KeyValue(existingBox.scaleYProperty(), 0.95)),
                    new KeyFrame(Duration.seconds(0.4), new KeyValue(existingBox.scaleYProperty(), 1))
            );
            welcomeNod.setDelay(Duration.seconds(i * 0.1));
            welcomeNod.play();
        }

        // 3. Dramatic entrance for the NEW player
        newPlayerBox.setOpacity(0);
        newPlayerBox.setScaleX(0.3);
        newPlayerBox.setScaleY(0.3);
        newPlayerBox.setRotate(-15);

        // Spectacular entrance animation
        Timeline newPlayerEntrance = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(newPlayerBox.opacityProperty(), 0),
                        new KeyValue(newPlayerBox.scaleXProperty(), 0.3),
                        new KeyValue(newPlayerBox.scaleYProperty(), 0.3),
                        new KeyValue(newPlayerBox.rotateProperty(), -15)),
                new KeyFrame(Duration.seconds(0.6),
                        new KeyValue(newPlayerBox.opacityProperty(), 1),
                        new KeyValue(newPlayerBox.scaleXProperty(), 1.2),
                        new KeyValue(newPlayerBox.scaleYProperty(), 1.2),
                        new KeyValue(newPlayerBox.rotateProperty(), 5)),
                new KeyFrame(Duration.seconds(1.0),
                        new KeyValue(newPlayerBox.scaleXProperty(), 1),
                        new KeyValue(newPlayerBox.scaleYProperty(), 1),
                        new KeyValue(newPlayerBox.rotateProperty(), 0))
        );

        // 4. Celebration ring expansion
        Timeline ringAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(celebrationRing.radiusProperty(), 0),
                        new KeyValue(celebrationRing.opacityProperty(), 0.8)),
                new KeyFrame(Duration.seconds(0.8),
                        new KeyValue(celebrationRing.radiusProperty(), 60),
                        new KeyValue(celebrationRing.opacityProperty(), 0.3)),
                new KeyFrame(Duration.seconds(1.2),
                        new KeyValue(celebrationRing.radiusProperty(), 100),
                        new KeyValue(celebrationRing.opacityProperty(), 0))
        );

        // 5. Center label celebration
        String celebrationText = "Player " + playerInfos.get(newPlayerIndex).getName() + " Joined!";
        Label celebrationLabel = new Label(celebrationText);
        celebrationLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        celebrationLabel.setTextFill(Color.GOLD);
        celebrationLabel.setStyle("-fx-background-color: rgba(255, 215, 0, 0.2); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 15; " +
                "-fx-border-color: #FFD700; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 15;");
        celebrationLabel.setOpacity(0);
        celebrationLabel.setScaleX(0.5);
        celebrationLabel.setScaleY(0.5);

        // Position celebration label above center label
        celebrationLabel.layoutXProperty().bind(centerLabel.layoutXProperty());
        celebrationLabel.layoutYProperty().bind(centerLabel.layoutYProperty().subtract(80));

        getChildren().add(celebrationLabel);

        // Celebration label animation
        Timeline celebrationLabelAnim = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(celebrationLabel.opacityProperty(), 0),
                        new KeyValue(celebrationLabel.scaleXProperty(), 0.5),
                        new KeyValue(celebrationLabel.scaleYProperty(), 0.5),
                        new KeyValue(celebrationLabel.translateYProperty(), 20)),
                new KeyFrame(Duration.seconds(0.4),
                        new KeyValue(celebrationLabel.opacityProperty(), 1),
                        new KeyValue(celebrationLabel.scaleXProperty(), 1),
                        new KeyValue(celebrationLabel.scaleYProperty(), 1),
                        new KeyValue(celebrationLabel.translateYProperty(), 0)),
                new KeyFrame(Duration.seconds(2.5),
                        new KeyValue(celebrationLabel.opacityProperty(), 1)),
                new KeyFrame(Duration.seconds(3.5),
                        new KeyValue(celebrationLabel.opacityProperty(), 0),
                        new KeyValue(celebrationLabel.translateYProperty(), -30))
        );

        // 6. Screen pulse effect
        Timeline screenPulse = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(scaleXProperty(), 1), new KeyValue(scaleYProperty(), 1)),
                new KeyFrame(Duration.seconds(0.3), new KeyValue(scaleXProperty(), 1.02), new KeyValue(scaleYProperty(), 1.02)),
                new KeyFrame(Duration.seconds(0.6), new KeyValue(scaleXProperty(), 1), new KeyValue(scaleYProperty(), 1))
        );

        // Play all animations
        newPlayerEntrance.setDelay(Duration.seconds(0.3));
        ringAnimation.setDelay(Duration.seconds(0.4));
        celebrationLabelAnim.setDelay(Duration.seconds(0.6));
        screenPulse.setDelay(Duration.seconds(0.5));

        newPlayerEntrance.play();
        ringAnimation.play();
        celebrationLabelAnim.play();
        screenPulse.play();

        // Clean up celebration elements
        Timeline cleanup = new Timeline(
                new KeyFrame(Duration.seconds(4), e -> {
                    getChildren().remove(celebrationRing);
                    getChildren().remove(celebrationLabel);
                })
        );
        cleanup.play();

        // Update waiting text after celebration
        updateWaitingText(playerInfos.size(), Duration.seconds(1.5));
    }

    private void showInitialPlayerLoad(List<PlayerInfo> playerInfos) {
        // Original staggered entrance for initial load
        for (int i = 0; i < playerInfos.size(); i++) {
            NameBox nameBox = nameBoxes.get(i);
            nameBox.setOpacity(0);
            Timeline staggeredEntrance = new Timeline(
                    new KeyFrame(Duration.seconds(i * 0.15),
                            new KeyValue(nameBox.opacityProperty(), 0)),
                    new KeyFrame(Duration.seconds(i * 0.15 + 0.4),
                            new KeyValue(nameBox.opacityProperty(), 1))
            );
            staggeredEntrance.play();
        }

        updateWaitingText(playerInfos.size(), Duration.seconds(0.5));
    }

    private void updateWaitingText(int playerCount, Duration delay) {
        if (playerCount < 4) {
            Timeline delayedUpdate = new Timeline(
                    new KeyFrame(delay, e -> Platform.runLater(() -> {
                        Timeline textChangeAnimation = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(centerLabel.opacityProperty(), 1)),
                                new KeyFrame(Duration.seconds(0.2), new KeyValue(centerLabel.opacityProperty(), 0.5)),
                                new KeyFrame(Duration.seconds(0.4), new KeyValue(centerLabel.opacityProperty(), 1))
                        );
                        textChangeAnimation.setOnFinished(evt -> {
                            centerLabel.setText("Waiting for Players... (" + playerCount + "/4)");
                            centerLabel.setTextFill(Color.DARKBLUE);

                            if (playerCount == 3) {
                                // Almost ready - add excitement!
                                centerLabel.setTextFill(Color.DARKORANGE);
                                centerLabel.setText("One More Player Needed! (" + playerCount + "/4)");
                            }
                        });
                        textChangeAnimation.play();
                    }))
            );
            delayedUpdate.play();
        }
    }

    public void startGameCountdown() {
        stopWaitingAnimation();

        // Cancel any existing countdown
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        // Create dramatic entrance animation for countdown
        Timeline countdownEntrance = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(centerLabel.scaleXProperty(), 1),
                        new KeyValue(centerLabel.scaleYProperty(), 1)),
                new KeyFrame(Duration.seconds(0.3),
                        new KeyValue(centerLabel.scaleXProperty(), 1.2),
                        new KeyValue(centerLabel.scaleYProperty(), 1.2)),
                new KeyFrame(Duration.seconds(0.6),
                        new KeyValue(centerLabel.scaleXProperty(), 1),
                        new KeyValue(centerLabel.scaleYProperty(), 1))
        );
        countdownEntrance.play();

        // Create new timer
        countdownTimer = new Timer();
        final int[] countdown = {3};

        countdownTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (countdown[0] > 0) {
                        // Countdown animation with dramatic effects
                        centerLabel.setText("Game Commencing in " + countdown[0] + "...");
                        centerLabel.setTextFill(Color.DARKGREEN);
                        centerLabel.setStyle("-fx-background-color: rgba(144, 238, 144, 0.95); " +
                                "-fx-background-radius: 20; " +
                                "-fx-padding: 25; " +
                                "-fx-border-color: #228B22; " +
                                "-fx-border-width: 4; " +
                                "-fx-border-radius: 20; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 20, 0, 0, 3);");

                        // Create pulsing effect for each countdown number
                        Timeline countdownPulse = new Timeline(
                                new KeyFrame(Duration.ZERO,
                                        new KeyValue(centerLabel.scaleXProperty(), 1),
                                        new KeyValue(centerLabel.scaleYProperty(), 1),
                                        new KeyValue(centerLabel.opacityProperty(), 1)),
                                new KeyFrame(Duration.seconds(0.2),
                                        new KeyValue(centerLabel.scaleXProperty(), 1.3),
                                        new KeyValue(centerLabel.scaleYProperty(), 1.3),
                                        new KeyValue(centerLabel.opacityProperty(), 0.8)),
                                new KeyFrame(Duration.seconds(0.6),
                                        new KeyValue(centerLabel.scaleXProperty(), 1),
                                        new KeyValue(centerLabel.scaleYProperty(), 1),
                                        new KeyValue(centerLabel.opacityProperty(), 1))
                        );

                        // Add screen shake effect for dramatic countdown
                        Timeline shakeEffect = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(translateXProperty(), 0)),
                                new KeyFrame(Duration.seconds(0.1), new KeyValue(translateXProperty(), -2)),
                                new KeyFrame(Duration.seconds(0.2), new KeyValue(translateXProperty(), 2)),
                                new KeyFrame(Duration.seconds(0.3), new KeyValue(translateXProperty(), 0))
                        );

                        countdownPulse.play();
                        if (countdown[0] <= 2) { // Only shake for final 2 seconds
                            shakeEffect.play();
                        }

                        countdown[0]--;
                    } else {
                        // Final "Starting Game..." with spectacular animation
                        centerLabel.setText("Starting Game...");
                        centerLabel.setTextFill(Color.WHITE);
                        centerLabel.setStyle("-fx-background-color: linear-gradient(to bottom, #FF6B6B, #FF8E53); " +
                                "-fx-background-radius: 25; " +
                                "-fx-padding: 30; " +
                                "-fx-border-color: #FF4757; " +
                                "-fx-border-width: 4; " +
                                "-fx-border-radius: 25; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(255,71,87,0.6), 25, 0, 0, 5);");

                        // Dramatic final animation sequence
                        Timeline finalAnimation = new Timeline(
                                new KeyFrame(Duration.ZERO,
                                        new KeyValue(centerLabel.scaleXProperty(), 1),
                                        new KeyValue(centerLabel.scaleYProperty(), 1),
                                        new KeyValue(centerLabel.rotateProperty(), 0)),
                                new KeyFrame(Duration.seconds(0.5),
                                        new KeyValue(centerLabel.scaleXProperty(), 1.5),
                                        new KeyValue(centerLabel.scaleYProperty(), 1.5),
                                        new KeyValue(centerLabel.rotateProperty(), 5)),
                                new KeyFrame(Duration.seconds(1.0),
                                        new KeyValue(centerLabel.scaleXProperty(), 1.2),
                                        new KeyValue(centerLabel.scaleYProperty(), 1.2),
                                        new KeyValue(centerLabel.rotateProperty(), -2)),
                                new KeyFrame(Duration.seconds(1.5),
                                        new KeyValue(centerLabel.scaleXProperty(), 0.8),
                                        new KeyValue(centerLabel.scaleYProperty(), 0.8),
                                        new KeyValue(centerLabel.opacityProperty(), 0.7)),
                                new KeyFrame(Duration.seconds(2.0),
                                        new KeyValue(centerLabel.scaleXProperty(), 0),
                                        new KeyValue(centerLabel.scaleYProperty(), 0),
                                        new KeyValue(centerLabel.opacityProperty(), 0))
                        );

                        // Screen flash effect
                        Rectangle flashOverlay = new Rectangle();
                        flashOverlay.setFill(Color.WHITE);
                        flashOverlay.setOpacity(0);
                        flashOverlay.widthProperty().bind(centerLabel.widthProperty());
                        flashOverlay.heightProperty().bind(centerLabel.heightProperty());
                        getChildren().add(flashOverlay);

                        Timeline flashEffect = new Timeline(
                                new KeyFrame(Duration.ZERO, new KeyValue(flashOverlay.opacityProperty(), 0)),
                                new KeyFrame(Duration.seconds(0.1), new KeyValue(flashOverlay.opacityProperty(), 0.3)),
                                new KeyFrame(Duration.seconds(0.3), new KeyValue(flashOverlay.opacityProperty(), 0))
                        );

                        finalAnimation.play();
                        flashEffect.play();

                        // Remove flash overlay after animation
                        flashEffect.setOnFinished(e -> getChildren().remove(flashOverlay));

                        // Cancel the timer after showing "Starting Game..."
                        countdownTimer.cancel();
                    }
                });
            }
        };

        // Schedule the countdown task to run every 1 second, starting immediately
        countdownTimer.scheduleAtFixedRate(countdownTask, 0, 1000);
    }

    public void resetToWaiting() {
        // Stop all animations
        stopAllAnimations();

        // Cancel any running countdown
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        // Reset to waiting state with smooth transition
        Platform.runLater(() -> {
            Timeline resetAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(centerLabel.scaleXProperty(), centerLabel.getScaleX()),
                            new KeyValue(centerLabel.scaleYProperty(), centerLabel.getScaleY()),
                            new KeyValue(centerLabel.opacityProperty(), centerLabel.getOpacity())),
                    new KeyFrame(Duration.seconds(0.5),
                            new KeyValue(centerLabel.scaleXProperty(), 1),
                            new KeyValue(centerLabel.scaleYProperty(), 1),
                            new KeyValue(centerLabel.opacityProperty(), 1))
            );

            resetAnimation.setOnFinished(e -> {
                centerLabel.setText("Waiting for Players...");
                centerLabel.setTextFill(Color.DARKBLUE);
                centerLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.95); " +
                        "-fx-background-radius: 20; " +
                        "-fx-padding: 25; " +
                        "-fx-border-color: #2E86AB; " +
                        "-fx-border-width: 3; " +
                        "-fx-border-radius: 20; " +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 2);");
                startWaitingAnimation();
            });

            resetAnimation.play();
        });
    }

    private void stopAllAnimations() {
        if (pulseAnimation != null) {
            pulseAnimation.stop();
        }
        if (fadeAnimation != null) {
            fadeAnimation.stop();
        }
        if (scaleAnimation != null) {
            scaleAnimation.stop();
        }
        if (rotateTransition != null) {
            rotateTransition.stop();
        }
    }

    // Clean up method to cancel timers and stop animations when screen is closed
    public void cleanup() {
        stopAllAnimations();

        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }
    }
}