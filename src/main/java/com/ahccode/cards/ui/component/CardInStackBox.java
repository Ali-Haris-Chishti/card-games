package com.ahccode.cards.ui.component;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Team;
import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;

public class CardInStackBox extends CardBox {

    // Animation elements for celebrations
    private Timeline glowAnimation;
    private Timeline bounceAnimation;
    private Timeline scorePopAnimation;

    public CardInStackBox(List<Card> cards, Team team, int playerNumber) {
        super(cards, team, playerNumber, 1);
        initializeStackBox();
    }

    public CardInStackBox(List<Card> cards, Team team, int playerNumber, int overlap) {
        super(cards, team, playerNumber, overlap);
        initializeStackBox(true);
    }

    private void initializeStackBox(boolean end) {
        // Set size based on card overlap and count
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        double containerPadding = 20;
        setPrefSize(totalWidth + containerPadding * 2, CARD_HEIGHT + containerPadding * 2);
        setPadding(new Insets(containerPadding));

        // Background color with rounded corners
        Color teamColor = switch (team) {
            case TEAM_GREEN -> Color.rgb(0, 150, 0, 0.35);
            case TEAM_BLUE -> Color.rgb(0, 100, 255, 0.35);
            default -> Color.rgb(255, 50, 50, 0.35); // center: red-ish
        };
        setBackground(new Background(new BackgroundFill(teamColor, new CornerRadii(15), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(teamColor.darker(), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(2))));

        // Build the cards
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.setFitWidth(CARD_WIDTH);
            card.setFitHeight(CARD_HEIGHT);

            // Center the whole group by offsetting initial layoutX
            double layoutX = (getPrefWidth() - totalWidth) / 2 + i * CARD_OVERLAP;
            card.setLayoutX(layoutX);
            card.setLayoutY(containerPadding);

            if (end) {
                card.setLayoutY(getPadding().getTop());
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            }

            // Subtle shadow by default
            DropShadow shadow = new DropShadow(8, Color.gray(0.4));
            card.setEffect(shadow);

            getChildren().add(card);
        }
    }

    private void initializeStackBox() {
        initializeStackBox(false);
    }

    private void repaint() {
        // Set size based on card overlap and count
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        double containerPadding = 20;
        setPrefSize(totalWidth + containerPadding * 2, CARD_HEIGHT + containerPadding * 2);
        setPadding(new Insets(containerPadding));

        // Build the cards
        for (Card card : cards) {
            card.toFront();
        }
    }

    @Override
    public void addCard(Card card) {
        // Set card size and effects
        card.setFitWidth(CARD_WIDTH);
        card.setFitHeight(CARD_HEIGHT);
        DropShadow shadow = new DropShadow(8, Color.gray(0.4));
        card.setEffect(shadow);
        card.setCursor(Cursor.HAND);

        // Add to list
        cards.add(card);
        getChildren().add(card);

        // Get current layout size
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        setPrefWidth(totalWidth + 40); // 40 for padding on both sides

        // Target position in the box
        double targetX = (getPrefWidth() - totalWidth) / 2 + (cards.size() - 1) * CARD_OVERLAP;
        double targetY = getPadding().getTop();

        // Get scene coordinates (starting point)
        double sceneX = card.localToScene(0, 0).getX();
        double sceneY = card.localToScene(0, 0).getY();

        // Convert to this box’s local space
        double localX = sceneX - localToScene(0, 0).getX();
        double localY = sceneY - localToScene(0, 0).getY();

        // Set initial position
        card.setLayoutX(localX);
        card.setLayoutY(localY);

        // Animate with translate (so layout is not disturbed)
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), card);
        transition.setToX(targetX - localX);
        transition.setToY(targetY - localY);

        transition.setOnFinished(e -> {
            // Snap to final layout position
            card.setLayoutX(targetX);
            card.setLayoutY(targetY);
            card.setTranslateX(0);
            card.setTranslateY(0);
        });

        transition.play();
    }


    private Color getTeamCelebrationColor() {
        return switch (team) {
            case TEAM_GREEN -> Color.rgb(34, 139, 34); // Forest Green
            case TEAM_BLUE -> Color.rgb(30, 144, 255); // Dodger Blue
            default -> Color.rgb(220, 20, 60); // Crimson
        };
    }

    private void updateBorderColor(Color color) {
        setBorder(new Border(new BorderStroke(
                color,
                BorderStrokeStyle.SOLID,
                new CornerRadii(15),
                new BorderWidths(3) // Slightly thicker during celebration
        )));
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    public List<Card> getCards() {
        return cards;
    }

    public void removeCard(Card card) {
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
        card.setOnMousePressed(null);

        cards.remove(card);
        getChildren().remove(card);
        repaint();
    }

    // ==================== PUBLIC ANIMATION API ====================

    /**
     * Plays score animation based on score value
     * @param scoreChange - positive for gained points, negative for lost points
     */
    public void playScoreAnimation(int scoreChange) {
        if (scoreChange > 0) {
            playPositiveScoreAnimation(scoreChange);
        } else if (scoreChange < 0) {
            playNegativeScoreAnimation(Math.abs(scoreChange));
        }
        // No animation for zero score change
    }

    /**
     * Plays celebration animation for positive score changes
     * @param points - number of points gained (must be positive)
     */
    public void playPositiveScoreAnimation(int points) {
        if (points <= 0) return;

        // 1. STACK GLOW CELEBRATION
        startPositiveStackGlow();

        // 2. POSITIVE SCORE POPUP
        showPositiveScorePopup(points);

        // 3. VICTORY RIPPLE EFFECT
        createPositiveRipple();

        // 4. CELEBRATION BOUNCE
        startPositiveBounce();
    }

    /**
     * Plays penalty animation for negative score changes
     * @param points - number of points lost (will be treated as positive number)
     */
    public void playNegativeScoreAnimation(int points) {
        if (points <= 0) return;

        // 1. STACK PENALTY EFFECT
        startNegativeStackEffect();

        // 2. NEGATIVE SCORE POPUP
        showNegativeScorePopup(points);

        // 3. PENALTY SHOCKWAVE
        createNegativeShockwave();

        // 4. DISAPPOINTMENT SHAKE
        startNegativeShake();
    }

    // ==================== POSITIVE SCORE ANIMATIONS ====================

    private void startPositiveStackGlow() {
        // Create golden glow effect for the entire stack
        Glow stackGlow = new Glow();
        stackGlow.setLevel(0);

        // Glow animation sequence
        glowAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(stackGlow.levelProperty(), 0)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(stackGlow.levelProperty(), 0.8)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(stackGlow.levelProperty(), 0.4)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(stackGlow.levelProperty(), 0))
        );

        setEffect(stackGlow);
        glowAnimation.setOnFinished(e -> setEffect(null));
        glowAnimation.play();

        // Border flash effect (positive - brighter)
        Color celebrationColor = getTeamCelebrationColor();
        Timeline borderFlash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateBorderColor(celebrationColor.brighter())),
                new KeyFrame(Duration.millis(800), e -> resetBorderColor())
        );
        borderFlash.play();
    }

    private void showPositiveScorePopup(int points) {
        String text = points == 1 ? "+1 POINT!" : "+" + points + " POINTS!";
        Label scorePopup = createScorePopup(text, getTeamCelebrationColor(), Color.rgb(255, 255, 255, 0.95));

        // Center the popup within the box
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        // Animate from center outward
        double upwardDistance = 50;   // How far up the popup travels

        // Positive popup animation (bouncy and upward from center)
        scorePopAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scorePopup.opacityProperty(), 0),
                        new KeyValue(scorePopup.scaleXProperty(), 0.5),
                        new KeyValue(scorePopup.scaleYProperty(), 0.5),
                        new KeyValue(scorePopup.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(scorePopup.opacityProperty(), 1),
                        new KeyValue(scorePopup.scaleXProperty(), 1.2),
                        new KeyValue(scorePopup.scaleYProperty(), 1.2),
                        new KeyValue(scorePopup.translateYProperty(), -upwardDistance * 0.6)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(scorePopup.scaleXProperty(), 1),
                        new KeyValue(scorePopup.scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(1500),
                        new KeyValue(scorePopup.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(2000),
                        new KeyValue(scorePopup.opacityProperty(), 0),
                        new KeyValue(scorePopup.translateYProperty(), -upwardDistance))
        );

        scorePopAnimation.setOnFinished(e -> getChildren().remove(scorePopup));
        scorePopAnimation.play();
    }

    private void createPositiveRipple() {
        Circle ripple = new Circle(0);
        ripple.setFill(Color.TRANSPARENT);
        ripple.setStroke(getTeamCelebrationColor());
        ripple.setStrokeWidth(3);
        ripple.setOpacity(0.8);

        ripple.setCenterX(getWidth() / 2);
        ripple.setCenterY(getHeight() / 2);
        getChildren().add(ripple);

        Timeline rippleAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(ripple.radiusProperty(), 0),
                        new KeyValue(ripple.opacityProperty(), 0.8)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(ripple.radiusProperty(), Math.max(getWidth(), getHeight()) * 0.7),
                        new KeyValue(ripple.opacityProperty(), 0.3)),
                new KeyFrame(Duration.millis(1000),
                        new KeyValue(ripple.radiusProperty(), Math.max(getWidth(), getHeight()) * 1.2),
                        new KeyValue(ripple.opacityProperty(), 0))
        );

        rippleAnimation.setOnFinished(e -> getChildren().remove(ripple));
        rippleAnimation.play();
    }

    private void startPositiveBounce() {
        bounceAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(scaleXProperty(), 1.05),
                        new KeyValue(scaleYProperty(), 1.05)),
                new KeyFrame(Duration.millis(500),
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1))
        );
        bounceAnimation.play();
    }

    // ==================== NEGATIVE SCORE ANIMATIONS ====================

    private void startNegativeStackEffect() {
        // Dark, ominous glow effect
        DropShadow penaltyShadow = new DropShadow(20, Color.DARKRED);
        penaltyShadow.setSpread(0.3);

        Timeline penaltyGlow = new Timeline(
                new KeyFrame(Duration.ZERO, e -> setEffect(penaltyShadow)),
                new KeyFrame(Duration.millis(150), e -> setEffect(null)),
                new KeyFrame(Duration.millis(300), e -> setEffect(penaltyShadow)),
                new KeyFrame(Duration.millis(450), e -> setEffect(null)),
                new KeyFrame(Duration.millis(600), e -> setEffect(penaltyShadow)),
                new KeyFrame(Duration.millis(1000), e -> setEffect(null))
        );
        penaltyGlow.play();

        // Border flash effect (negative - darker red)
        Timeline borderFlash = new Timeline(
                new KeyFrame(Duration.ZERO, e -> updateBorderColor(Color.DARKRED)),
                new KeyFrame(Duration.millis(200), e -> updateBorderColor(Color.RED)),
                new KeyFrame(Duration.millis(400), e -> updateBorderColor(Color.DARKRED)),
                new KeyFrame(Duration.millis(800), e -> resetBorderColor())
        );
        borderFlash.play();
    }

    private void showNegativeScorePopup(int points) {
        String text = points == 1 ? "-1 POINT!" : "-" + points + " POINTS!";
        Label scorePopup = createScorePopup(text, Color.DARKRED, Color.rgb(255, 200, 200, 0.95));

        // Center the popup within the box
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        // Animate from center downward
        double downwardDistance = 40;   // How far down the popup travels

        // Negative popup animation (heavy and downward from center)
        scorePopAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scorePopup.opacityProperty(), 0),
                        new KeyValue(scorePopup.scaleXProperty(), 1.2),
                        new KeyValue(scorePopup.scaleYProperty(), 0.3),
                        new KeyValue(scorePopup.translateYProperty(), 0)),
                new KeyFrame(Duration.millis(200),
                        new KeyValue(scorePopup.opacityProperty(), 1),
                        new KeyValue(scorePopup.scaleXProperty(), 1),
                        new KeyValue(scorePopup.scaleYProperty(), 1),
                        new KeyValue(scorePopup.translateYProperty(), downwardDistance * 0.4)),
                new KeyFrame(Duration.millis(1200),
                        new KeyValue(scorePopup.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(1800),
                        new KeyValue(scorePopup.opacityProperty(), 0),
                        new KeyValue(scorePopup.translateYProperty(), downwardDistance))
        );

        scorePopAnimation.setOnFinished(e -> getChildren().remove(scorePopup));
        scorePopAnimation.play();
    }

    private void createNegativeShockwave() {
        // Multiple concentric circles for shockwave effect
        for (int i = 0; i < 3; i++) {
            Circle shockwave = new Circle(0);
            shockwave.setFill(Color.TRANSPARENT);
            shockwave.setStroke(Color.DARKRED);
            shockwave.setStrokeWidth(2);
            shockwave.setOpacity(0.6);

            shockwave.setCenterX(getWidth() / 2);
            shockwave.setCenterY(getHeight() / 2);
            getChildren().add(shockwave);

            Timeline shockwaveAnimation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(shockwave.radiusProperty(), 10 + i * 15),
                            new KeyValue(shockwave.opacityProperty(), 0.6)),
                    new KeyFrame(Duration.millis(400 + i * 100),
                            new KeyValue(shockwave.radiusProperty(), Math.max(getWidth(), getHeight()) * 0.8),
                            new KeyValue(shockwave.opacityProperty(), 0))
            );

            final Circle finalShockwave = shockwave;
            shockwaveAnimation.setOnFinished(e -> getChildren().remove(finalShockwave));
            shockwaveAnimation.play();
        }
    }

    private void startNegativeShake() {
        // Frustrated shake animation
        Timeline shakeAnimation = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(translateXProperty(), -3)),
                new KeyFrame(Duration.millis(100), new KeyValue(translateXProperty(), 3)),
                new KeyFrame(Duration.millis(150), new KeyValue(translateXProperty(), -2)),
                new KeyFrame(Duration.millis(200), new KeyValue(translateXProperty(), 2)),
                new KeyFrame(Duration.millis(250), new KeyValue(translateXProperty(), -1)),
                new KeyFrame(Duration.millis(300), new KeyValue(translateXProperty(), 1)),
                new KeyFrame(Duration.millis(350), new KeyValue(translateXProperty(), 0))
        );

        // Stack compression effect (showing "weight" of penalty)
        Timeline compressionAnimation = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1)),
                new KeyFrame(Duration.millis(150),
                        new KeyValue(scaleXProperty(), 0.95),
                        new KeyValue(scaleYProperty(), 0.98)),
                new KeyFrame(Duration.millis(400),
                        new KeyValue(scaleXProperty(), 1),
                        new KeyValue(scaleYProperty(), 1))
        );

        shakeAnimation.play();
        compressionAnimation.play();
    }

    // ==================== HELPER METHODS ====================

    private Label createScorePopup(String text, Color textColor, Color backgroundColor) {
        Label scorePopup = new Label(text);
        scorePopup.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        scorePopup.setTextFill(textColor);
        scorePopup.setStyle("-fx-background-color: " + toRgbaString(backgroundColor) + "; " +
                "-fx-background-radius: 12; " +
                "-fx-padding: 8 15 8 15; " +
                "-fx-border-color: " + toHexString(textColor) + "; " +
                "-fx-border-width: 2; " +
                "-fx-border-radius: 12; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 10, 0, 0, 3);");

        // Center the popup within the box bounds
        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        // Estimate label size for better centering (rough approximation)
        double estimatedLabelWidth = text.length() * 10 + 30; // rough estimate
        double estimatedLabelHeight = 30;

        scorePopup.setLayoutX(centerX - estimatedLabelWidth / 2);
        scorePopup.setLayoutY(centerY - estimatedLabelHeight / 2);
        scorePopup.setOpacity(0);

        getChildren().add(scorePopup);
        return scorePopup;
    }

    private void resetBorderColor() {
        Color teamColor = switch (team) {
            case TEAM_GREEN -> Color.rgb(0, 150, 0, 0.35);
            case TEAM_BLUE -> Color.rgb(0, 100, 255, 0.35);
            default -> Color.rgb(255, 50, 50, 0.35);
        };
        updateBorderColor(teamColor.darker());
    }

    private String toRgbaString(Color color) {
        return String.format("rgba(%d, %d, %d, %.2f)",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                color.getOpacity());
    }

    // Clean up animations when component is destroyed
    public void cleanup() {
        if (glowAnimation != null) {
            glowAnimation.stop();
        }
        if (bounceAnimation != null) {
            bounceAnimation.stop();
        }
        if (scorePopAnimation != null) {
            scorePopAnimation.stop();
        }
    }
}