package com.ahccode.cards.ui;

import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.card.game.PlayerInfo;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.context.GameContextUI;
import com.ahccode.cards.ui.component.NameBox;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerWaitingScreen extends Pane {

    List<NameBox> nameBoxes;
    private Label centerLabel;
    private Timer countdownTimer;
    private TimerTask countdownTask;

    private static PlayerWaitingScreen instance;

    private PlayerWaitingScreen() {
        initializeCenterLabel();
    }

    private void initializeCenterLabel() {
        centerLabel = new Label("Waiting for Players...");
        centerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        centerLabel.setTextFill(Color.DARKBLUE);
        centerLabel.setTextAlignment(TextAlignment.CENTER);
        centerLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                "-fx-background-radius: 15; " +
                "-fx-padding: 20; " +
                "-fx-border-color: #2E86AB; " +
                "-fx-border-width: 3; " +
                "-fx-border-radius: 15; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

        getChildren().add(centerLabel);
    }

    public void initialize() {
        GameContextCore.currentPlayer.sendHelloToServer();
        updatePlayerInfoList(new ArrayList<>());
    }

    @Override
    protected void layoutChildren() {
        double padding = 20;
        double spacing = 10;

        double width = getWidth();
        double height = getHeight();

        // Position center label first
        double centerLabelWidth = centerLabel.prefWidth(-1);
        double centerLabelHeight = centerLabel.prefHeight(-1);
        centerLabel.resizeRelocate(
                (width - centerLabelWidth) / 2,
                (height - centerLabelHeight) / 2,
                centerLabelWidth,
                centerLabelHeight
        );

        // Hand boxes (4 players)
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

            // Position hand boxes
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
        nameBoxes.add(new NameBox(info.getPlayerNumber() % 2 == 0? Team.TEAM_GREEN: Team.TEAM_BLUE, info.getName()));
    }

    public void updatePlayerInfoList(List<PlayerInfo> playerInfos) {
        // Clear previous nameboxes but keep the center label
        getChildren().removeIf(child -> child instanceof NameBox);

        nameBoxes = new ArrayList<>(4);
        for (int i = 0; i < playerInfos.size(); i++) {
            nameBoxes.add(new NameBox(i % 2 == 0? Team.TEAM_GREEN: Team.TEAM_BLUE, playerInfos.get(i).getName()));
            getChildren().add(nameBoxes.get(i));
        }
        for (int i = playerInfos.size(); i < 4; i++) {
            nameBoxes.add(new NameBox());
            getChildren().add(nameBoxes.get(i));
        }

        // Update center text based on player count
        if (playerInfos.size() < 4) {
            Platform.runLater(() -> {
                centerLabel.setText("Waiting for Players... (" + playerInfos.size() + "/4)");
                centerLabel.setTextFill(Color.DARKBLUE);
            });
        }
    }

    public void startGameCountdown() {
        // Cancel any existing countdown
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        // Create new timer
        countdownTimer = new Timer();

        // Start countdown from 3
        final int[] countdown = {3};

        countdownTask = new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (countdown[0] > 0) {
                        centerLabel.setText("Game Commencing in " + countdown[0] + "...");
                        centerLabel.setTextFill(Color.DARKGREEN);
                        centerLabel.setStyle("-fx-background-color: rgba(144, 238, 144, 0.9); " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 20; " +
                                "-fx-border-color: #228B22; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 15; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
                        countdown[0]--;
                    } else {
                        centerLabel.setText("Starting Game...");
                        centerLabel.setTextFill(Color.DARKRED);
                        centerLabel.setStyle("-fx-background-color: rgba(255, 182, 193, 0.9); " +
                                "-fx-background-radius: 15; " +
                                "-fx-padding: 20; " +
                                "-fx-border-color: #DC143C; " +
                                "-fx-border-width: 3; " +
                                "-fx-border-radius: 15; " +
                                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");

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
        // Cancel any running countdown
        if (countdownTimer != null) {
            countdownTimer.cancel();
        }
        if (countdownTask != null) {
            countdownTask.cancel();
        }

        // Reset to waiting state
        Platform.runLater(() -> {
            centerLabel.setText("Waiting for Players...");
            centerLabel.setTextFill(Color.DARKBLUE);
            centerLabel.setStyle("-fx-background-color: rgba(255, 255, 255, 0.9); " +
                    "-fx-background-radius: 15; " +
                    "-fx-padding: 20; " +
                    "-fx-border-color: #2E86AB; " +
                    "-fx-border-width: 3; " +
                    "-fx-border-radius: 15; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        });
    }

    // Clean up method to cancel timers when screen is closed
    public void cleanup() {
        if (countdownTimer != null) {
            countdownTimer.cancel();
//            countdownTimer = null;
        }
        if (countdownTask != null) {
            countdownTask.cancel();
//            countdownTask = null;
        }
    }
}