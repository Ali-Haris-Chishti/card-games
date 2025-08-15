package com.ahccode.cards.ui.daketi;

import com.ahccode.cards.ClientMainFX;
import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.ui.component.CardInStackBox;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class DaketiGameFinishedScreen extends StackPane {

    public enum GameResult {
        WIN, DRAW, LOSE
    }

    // Getter methods for button event handling
    @Getter
    private Button leaveButton;
    private Button playAgainButton;
    private GameResult playerResult;

    public DaketiGameFinishedScreen(List<Card> teamAStack, List<Card> teamBStack, int teamAScore, int teamBScore, GameResult playerResult) {
        try {
            log.info("Sending CLose Game message");
            GameContextCore.currentPlayer.getAssociatedClient().send(new Message(MessageType.GAME_FINISHED, null));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.playerResult = playerResult;

        String resultText;
        Color resultColor;
        Color backgroundColor1, backgroundColor2;

        switch (playerResult) {
            case WIN -> {
                resultText = "🎉 VICTORY! 🎉\nYou Won!";
                resultColor = Color.YELLOW;
                backgroundColor1 = Color.rgb(34, 139, 34, 0.3);  // Forest Green
                backgroundColor2 = Color.rgb(50, 205, 50, 0.1);  // Lime Green
            }
            case LOSE -> {
                resultText = "💔 DEFEAT 💔\nYou Lost!";
                resultColor = Color.DARKRED;
                backgroundColor1 = Color.rgb(139, 0, 0, 0.3);    // Dark Red
                backgroundColor2 = Color.rgb(220, 20, 60, 0.1);  // Crimson
            }
            case DRAW -> {
                resultText = "⚖️ DRAW ⚖️\nIt's a Tie!";
                resultColor = Color.DARKORANGE;
                backgroundColor1 = Color.rgb(255, 140, 0, 0.3);  // Dark Orange
                backgroundColor2 = Color.rgb(255, 215, 0, 0.1);  // Gold
            }
            default -> {
                resultText = "Game Over";
                resultColor = Color.DARKBLUE;
                backgroundColor1 = Color.rgb(240, 248, 255);
                backgroundColor2 = Color.rgb(220, 235, 250);
            }
        }

        Label resultLabel = new Label(resultText);
        resultLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        resultLabel.setTextFill(resultColor);
        resultLabel.setPadding(new Insets(20, 0, 30, 0));
        resultLabel.setStyle("-fx-text-alignment: center;");

        // Enhanced drop shadow based on result
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(playerResult == GameResult.WIN ? 10.0 : 5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(playerResult == GameResult.WIN ? Color.GOLD.deriveColor(0, 1, 1, 0.6) : Color.color(0.4, 0.5, 0.5));
        resultLabel.setEffect(dropShadow);

        CardInStackBox teamAStackBox = new CardInStackBox(teamAStack, Team.TEAM_GREEN, -1, 30);
        CardInStackBox teamBStackBox = new CardInStackBox(teamBStack, Team.TEAM_BLUE, -1, 30);

        Label teamALabel = new Label("Team Green Score: " + teamAScore);
        teamALabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        teamALabel.setTextFill(Color.DARKGREEN);

        Label teamBLabel = new Label("Team Blue Score: " + teamBScore);
        teamBLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        teamBLabel.setTextFill(Color.DARKBLUE);

        // Create team sections with centered card boxes
        VBox teamASection = new VBox(15);
        teamASection.setAlignment(Pos.CENTER);

        // Center the team A stack box horizontally
        HBox teamABoxContainer = new HBox(teamAStackBox);
        teamABoxContainer.setAlignment(Pos.CENTER);

        teamASection.getChildren().addAll(teamABoxContainer, teamALabel);

        VBox teamBSection = new VBox(15);
        teamBSection.setAlignment(Pos.CENTER);

        // Center the team B stack box horizontally
        HBox teamBBoxContainer = new HBox(teamBStackBox);
        teamBBoxContainer.setAlignment(Pos.CENTER);

        teamBSection.getChildren().addAll(teamBBoxContainer, teamBLabel);

        // Create buttons
        createButtons();

        // Button container
        HBox buttonBox = new HBox(30, leaveButton, playAgainButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(30, 0, 20, 0));

        VBox mainBox = new VBox(25, resultLabel, teamASection, teamBSection, buttonBox);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(40));

        // Result-specific gradient background
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, backgroundColor1),
                new Stop(1, backgroundColor2)
        );
        setBackground(new Background(new BackgroundFill(gradient, CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().add(mainBox);

        // Play entrance animation
        playEntranceAnimation(resultLabel, teamASection, teamBSection, buttonBox);

        // Play result-specific animation
        playResultAnimation(resultLabel);
    }

    private void createButtons() {
        // Leave Button
        leaveButton = new Button("Leave Game");
        leaveButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        leaveButton.setPrefSize(140, 45);
        leaveButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff6b6b, #ee5a52);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        leaveButton.setOnAction(e -> System.exit(0));

        // Play Again Button
        playAgainButton = new Button("Play Again");
        playAgainButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        playAgainButton.setPrefSize(140, 45);
        playAgainButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4ecdc4, #44a08d);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        playAgainButton.setOnAction(e -> onPlayAgainButtonClicked());

        // Add hover effects
        addButtonHoverEffects(leaveButton);
        addButtonHoverEffects(playAgainButton);
    }

    private void onPlayAgainButtonClicked() {
        Platform.runLater(() -> ClientMainFX.restartGame(false));
    }

    private void addButtonHoverEffects(Button button) {
        button.setOnMouseEntered(e -> {
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(100), button);
            scaleIn.setToX(1.05);
            scaleIn.setToY(1.05);
            scaleIn.play();
        });

        button.setOnMouseExited(e -> {
            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(100), button);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);
            scaleOut.play();
        });
    }

    private void playEntranceAnimation(Label resultLabel, VBox teamASection, VBox teamBSection, HBox buttonBox) {
        // Initial setup - make everything invisible and positioned
        resultLabel.setOpacity(0);
        teamASection.setOpacity(0);
        teamBSection.setOpacity(0);
        buttonBox.setOpacity(0);

        resultLabel.setTranslateY(-50);
        teamASection.setTranslateX(-100);
        teamBSection.setTranslateX(100);
        buttonBox.setTranslateY(50);

        // Create timeline for sequential animations
        Timeline timeline = new Timeline();

        // Result label animation
        KeyFrame resultFrame1 = new KeyFrame(Duration.millis(200),
                new KeyValue(resultLabel.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(resultLabel.translateYProperty(), 0, Interpolator.EASE_OUT)
        );

        // Team sections animation
        KeyFrame teamFrame1 = new KeyFrame(Duration.millis(600),
                new KeyValue(teamASection.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(teamASection.translateXProperty(), 0, Interpolator.EASE_OUT)
        );

        KeyFrame teamFrame2 = new KeyFrame(Duration.millis(800),
                new KeyValue(teamBSection.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(teamBSection.translateXProperty(), 0, Interpolator.EASE_OUT)
        );

        // Buttons animation
        KeyFrame buttonFrame = new KeyFrame(Duration.millis(1200),
                new KeyValue(buttonBox.opacityProperty(), 1, Interpolator.EASE_OUT),
                new KeyValue(buttonBox.translateYProperty(), 0, Interpolator.EASE_OUT)
        );

        timeline.getKeyFrames().addAll(resultFrame1, teamFrame1, teamFrame2, buttonFrame);
        timeline.play();
    }

    private void playResultAnimation(Label resultLabel) {
        switch (playerResult) {
            case WIN -> playWinAnimation(resultLabel);
            case LOSE -> playLoseAnimation(resultLabel);
            case DRAW -> playDrawAnimation(resultLabel);
        }
    }

    private void playWinAnimation(Label resultLabel) {
        // Victory celebration - multiple effects

        // 1. Triumphant pulsing - INFINITE
        ScaleTransition pulse = new ScaleTransition(Duration.millis(600), resultLabel);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.15);
        pulse.setToY(1.15);
        pulse.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        pulse.setAutoReverse(true);
        pulse.setInterpolator(Interpolator.EASE_BOTH);

        // 2. Golden glow effect - INFINITE
        Timeline colorAnimation = new Timeline();
        Color goldColor = Color.GOLD;
        Color brightGold = Color.GOLD.brighter().brighter();

        KeyFrame colorFrame1 = new KeyFrame(Duration.millis(300),
                new KeyValue(resultLabel.textFillProperty(), brightGold)
        );
        KeyFrame colorFrame2 = new KeyFrame(Duration.millis(600),
                new KeyValue(resultLabel.textFillProperty(), goldColor)
        );

        colorAnimation.getKeyFrames().addAll(colorFrame1, colorFrame2);
        colorAnimation.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        colorAnimation.setAutoReverse(true);

        // 3. Slight bounce effect - INFINITE
        TranslateTransition bounce = new TranslateTransition(Duration.millis(800), resultLabel);
        bounce.setFromY(0);
        bounce.setToY(-15);
        bounce.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        bounce.setAutoReverse(true);
        bounce.setInterpolator(Interpolator.EASE_BOTH);

        // Play all animations
        pulse.setDelay(Duration.millis(500));
        colorAnimation.setDelay(Duration.millis(500));
        bounce.setDelay(Duration.millis(800));

        pulse.play();
        colorAnimation.play();
        bounce.play();

        // Trigger celebration sparkles with infinite repeating
        Timeline sparkleTimer = new Timeline(new KeyFrame(Duration.millis(3000), e -> playCelebrationAnimation()));
        sparkleTimer.setCycleCount(Timeline.INDEFINITE);
        sparkleTimer.setDelay(Duration.millis(1000));
        sparkleTimer.play();
    }

    private void playLoseAnimation(Label resultLabel) {
        // Defeat animation - somber and subdued

        // 1. Slow fade in/out - INFINITE
        FadeTransition fade = new FadeTransition(Duration.millis(1000), resultLabel);
        fade.setFromValue(1.0);
        fade.setToValue(0.7);
        fade.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        fade.setAutoReverse(true);
        fade.setInterpolator(Interpolator.EASE_BOTH);

        // 2. Slight downward drift - INFINITE
        TranslateTransition drift = new TranslateTransition(Duration.millis(1500), resultLabel);
        drift.setFromY(0);
        drift.setToY(10);
        drift.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        drift.setAutoReverse(true);
        drift.setInterpolator(Interpolator.EASE_IN);

        // 3. Color darkening - INFINITE
        Timeline colorAnimation = new Timeline();
        Color darkRed = Color.DARKRED;
        Color darkerRed = darkRed.darker();

        KeyFrame colorFrame1 = new KeyFrame(Duration.millis(500),
                new KeyValue(resultLabel.textFillProperty(), darkerRed)
        );
        KeyFrame colorFrame2 = new KeyFrame(Duration.millis(1000),
                new KeyValue(resultLabel.textFillProperty(), darkRed)
        );

        colorAnimation.getKeyFrames().addAll(colorFrame1, colorFrame2);
        colorAnimation.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        colorAnimation.setAutoReverse(true);

        // Play animations with delay
        fade.setDelay(Duration.millis(500));
        drift.setDelay(Duration.millis(700));
        colorAnimation.setDelay(Duration.millis(500));

        fade.play();
        drift.play();
        colorAnimation.play();
    }

    private void playDrawAnimation(Label resultLabel) {
        // Draw animation - balanced, neutral movement

        // 1. Gentle sway left and right - INFINITE
        RotateTransition sway = new RotateTransition(Duration.millis(1200), resultLabel);
        sway.setFromAngle(-8);
        sway.setToAngle(8);
        sway.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        sway.setAutoReverse(true);
        sway.setInterpolator(Interpolator.EASE_BOTH);

        // 2. Color shifting between orange tones - INFINITE
        Timeline colorAnimation = new Timeline();
        Color orange = Color.DARKORANGE;
        Color brightOrange = Color.ORANGE;

        KeyFrame colorFrame1 = new KeyFrame(Duration.millis(600),
                new KeyValue(resultLabel.textFillProperty(), brightOrange)
        );
        KeyFrame colorFrame2 = new KeyFrame(Duration.millis(1200),
                new KeyValue(resultLabel.textFillProperty(), orange)
        );

        colorAnimation.getKeyFrames().addAll(colorFrame1, colorFrame2);
        colorAnimation.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        colorAnimation.setAutoReverse(true);

        // 3. Gentle scale pulsing - INFINITE
        ScaleTransition scale = new ScaleTransition(Duration.millis(800), resultLabel);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(1.05);
        scale.setToY(1.05);
        scale.setCycleCount(Timeline.INDEFINITE); // Changed to infinite
        scale.setAutoReverse(true);
        scale.setInterpolator(Interpolator.EASE_BOTH);

        // Play animations
        sway.setDelay(Duration.millis(500));
        colorAnimation.setDelay(Duration.millis(500));
        scale.setDelay(Duration.millis(600));

        sway.play();
        colorAnimation.play();
        scale.play();
    }

    public Button getPlayAgainButton() {
        return playAgainButton;
    }

    // Method to play celebration animation when called externally
    public void playCelebrationAnimation() {
        // Firework-like effect using multiple small animations
        for (int i = 0; i < 10; i++) {
            Label sparkle = new Label("✨");
            sparkle.setFont(Font.font(20));
            sparkle.setTextFill(Color.GOLD);

            // Random position
            double x = Math.random() * getWidth();
            double y = Math.random() * getHeight();
            sparkle.setLayoutX(x);
            sparkle.setLayoutY(y);

            getChildren().add(sparkle);

            // Animate sparkle
            FadeTransition fade = new FadeTransition(Duration.millis(2000), sparkle);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);

            ScaleTransition scale = new ScaleTransition(Duration.millis(2000), sparkle);
            scale.setFromX(0.5);
            scale.setFromY(0.5);
            scale.setToX(2.0);
            scale.setToY(2.0);

            ParallelTransition sparkleAnimation = new ParallelTransition(fade, scale);
            sparkleAnimation.setDelay(Duration.millis(i * 200));
            sparkleAnimation.setOnFinished(e -> getChildren().remove(sparkle));
            sparkleAnimation.play();
        }
    }
}