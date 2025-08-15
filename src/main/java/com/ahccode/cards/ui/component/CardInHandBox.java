package com.ahccode.cards.ui.component;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.GameController;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.ui.controller.DaketiScreenController;
import com.ahccode.cards.ui.controller.ScreenController;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class CardInHandBox extends CardBox {

    private Label playerNameLabel;
    private static final double NAME_LABEL_HEIGHT = 35;
    private static final double NAME_LABEL_PADDING = 10;

    public CardInHandBox(List<Card> cards, Team team, int playerNumber, String playerName) {
        super(cards, team, playerNumber, 30);
        initializeBox(playerName);
    }

    private void initializeBox(String playerName) {
        // Set size based on card overlap and count + space for name label (only if name is provided)
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        double containerPadding = 20;
        boolean hasPlayerName = playerName != null && !playerName.trim().isEmpty();
        double totalHeight = CARD_HEIGHT + containerPadding * 2 + (hasPlayerName ? NAME_LABEL_HEIGHT + NAME_LABEL_PADDING : 0);

        setPrefSize(totalWidth + containerPadding * 2, totalHeight);
        setPadding(new Insets(containerPadding));

        // Background color with rounded corners
        Color teamColor = switch (team) {
            case TEAM_GREEN -> Color.rgb(0, 150, 0, 0.35);
            case TEAM_BLUE -> Color.rgb(0, 100, 255, 0.35);
            default -> Color.rgb(255, 50, 50, 0.35); // center: red-ish
        };
        setBackground(new Background(new BackgroundFill(teamColor, new CornerRadii(15), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(teamColor.darker(), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(2))));

        // Create and add player name label only if name is provided
        if (hasPlayerName) {
            playerNameLabel = new Label(playerName);
            playerNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            playerNameLabel.setTextFill(Color.WHITE);
            playerNameLabel.setAlignment(Pos.CENTER);
            playerNameLabel.setPrefWidth(totalWidth);
            playerNameLabel.setPrefHeight(NAME_LABEL_HEIGHT);

            // Position name label at bottom of the box
            playerNameLabel.setLayoutX(containerPadding);
            playerNameLabel.setLayoutY(getPrefHeight() - NAME_LABEL_HEIGHT - containerPadding);

            getChildren().add(playerNameLabel);
        }

        log.info("Cards In Hand: {}", cards);
        // Build the cards
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.setFitWidth(CARD_WIDTH);
            card.setFitHeight(CARD_HEIGHT);

            // Center the whole group by offsetting initial layoutX
            double layoutX = (getPrefWidth() - totalWidth) / 2 + i * CARD_OVERLAP;
            card.setLayoutX(layoutX);
            card.setLayoutY(containerPadding); // Cards stay at top

            // Hover effects
            card.setOnMouseEntered(e -> handleMouseEnter(card));
            card.setOnMouseExited(e -> handleMouseExit(card, false));

            if (playerNumber != -1 && playerNumber == GameContextCore.currentPlayer.getPlayerNumber()) {
                log.info("Handling Mouse event for Player {}", GameContextCore.currentPlayer.getPlayerNumber());
                card.setOnMouseReleased(e -> {
                    handleMousePressed(card, playerNumber);
                });
                card.setCursor(Cursor.HAND);
            }

            // Subtle shadow by default
            DropShadow shadow = new DropShadow(8, Color.gray(0.4));
            card.setEffect(shadow);

            log.info("Added Card: {}", card);
            getChildren().add(card);

        }
//        playByItself();
    }

    public void rearrange() {
        animationFlag = false;

        // Recalculate size based on current card count
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        double containerPadding = 20;
        boolean hasPlayerName = playerNameLabel != null;
        double totalHeight = CARD_HEIGHT + containerPadding * 2 + (hasPlayerName ? NAME_LABEL_HEIGHT + NAME_LABEL_PADDING : 0);

        setPrefSize(totalWidth + containerPadding * 2, totalHeight);
        setPadding(new Insets(containerPadding));

        // Update background
        Color teamColor = switch (team) {
            case TEAM_GREEN -> Color.rgb(0, 150, 0, 0.35);
            case TEAM_BLUE -> Color.rgb(0, 100, 255, 0.35);
            default -> Color.rgb(255, 50, 50, 0.35); // center: red-ish
        };
        setBackground(new Background(new BackgroundFill(teamColor, new CornerRadii(15), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(teamColor.darker(), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(2))));

        // Update name label position and size only if it exists
        if (hasPlayerName) {
            playerNameLabel.setPrefWidth(totalWidth);
            playerNameLabel.setLayoutX(containerPadding);
            playerNameLabel.setLayoutY(getPrefHeight() - NAME_LABEL_HEIGHT - containerPadding);
        }

        // Rearrange cards
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);

            // Center the whole group by offsetting initial layoutX
            double layoutX = (getPrefWidth() - totalWidth) / 2 + i * CARD_OVERLAP;
            card.setLayoutX(layoutX);
            card.setLayoutY(containerPadding);
            handleMouseExit(card, true);

            // Hover effects
            card.setOnMouseEntered(e -> handleMouseEnter(card));
            card.setOnMouseExited(e -> handleMouseExit(card, false));

            if (playerNumber != -1 && playerNumber == GameContextCore.currentPlayer.getPlayerNumber()) {
                card.setCursor(Cursor.HAND);
                card.setOnMousePressed(e -> {
                    log.info("Handling Mouse event for Player {}", GameContextCore.currentPlayer.getPlayerNumber());
                    handleMousePressed(card, playerNumber);
                });
            }
            card.toFront();

            // Subtle shadow by default
            DropShadow shadow = new DropShadow(8, Color.gray(0.4));
            card.setEffect(shadow);

            log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Color Restored");
        }

        // Ensure name label stays on top (only if it exists)
        if (playerNameLabel != null) {
            playerNameLabel.toFront();
        }
    }

    public void addCard(Card card) {
        // Add to list
        cards.add(card);
        getChildren().add(card);

        // Get current layout size (recalculate for new card count)
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        double containerPadding = 20;
        boolean hasPlayerName = playerNameLabel != null;
        double totalHeight = CARD_HEIGHT + containerPadding * 2 + (hasPlayerName ? NAME_LABEL_HEIGHT + NAME_LABEL_PADDING : 0);

        setPrefSize(totalWidth + containerPadding * 2, totalHeight);

        // Update name label only if it exists
        if (hasPlayerName) {
            playerNameLabel.setPrefWidth(totalWidth);
            playerNameLabel.setLayoutX(containerPadding);
            playerNameLabel.setLayoutY(getPrefHeight() - NAME_LABEL_HEIGHT - containerPadding);
        }

        // Animate card from current position to its new one
        double targetX = (getPrefWidth() - totalWidth) / 2 + (cards.size() - 1) * CARD_OVERLAP;
        double targetY = containerPadding; // Cards stay at top

        // Get scene coordinates (starting point)
        double sceneX = card.localToScene(0, 0).getX();
        double sceneY = card.localToScene(0, 0).getY();

        // Move it to this box's coordinate space
        double localX = sceneX - localToScene(0, 0).getX();
        double localY = sceneY - localToScene(0, 0).getY();

        card.setLayoutX(localX);
        card.setLayoutY(localY);

        // Animate to new position
        TranslateTransition transition = new TranslateTransition(Duration.millis(300), card);
        transition.setToX(targetX - localX);
        transition.setToY(targetY - localY);
        transition.setOnFinished(e -> {
            card.setLayoutX(targetX);
            card.setLayoutY(targetY);
            card.setTranslateX(0);
            card.setTranslateY(0);
            rearrange();
            animationFlag = true;
        });
        transition.play();
    }

    // ... rest of the existing methods remain the same ...

    private void handleMousePressed(Card card, int playerNumber) {
        animationFlag = false;
        screenController.cardClicked(card, playerNumber);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                animationFlag = true;
            }
        }, 100);
    }

    private void playByItself() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (playerNumber == GameContextCore.turn)
                    screenController.cardClicked(cards.get(new Random().nextInt(0, cards.size())), playerNumber);
            }
        }, 1000, 1500);
    }

    private void handleMouseEnter(ImageView cardView) {
        if (!animationFlag)
            return;
        cardView.toFront();
        cardView.setLayoutY(cardView.getLayoutY() - 10); // raise it a bit
        cardView.setScaleX(1.07);
        cardView.setScaleY(1.07);
        cardView.setEffect(new DropShadow(20, Color.BLACK));
    }

    private void handleMouseExit(ImageView cardView, boolean cross) {
        // Short Circuit effect, if cross true, then animationFlag check will be neglected
        if (cross || animationFlag) {
            cardView.setLayoutY(getPadding().getTop());
            cardView.setScaleX(1.0);
            cardView.setScaleY(1.0);
            DropShadow defaultShadow = new DropShadow(8, Color.gray(0.4));
            cardView.setEffect(defaultShadow);
            setInitialPattern();
        }
    }

    private void setInitialPattern() {
        for (Card card : cards) {
            card.toFront();
        }
        // Keep name label visible (only if it exists)
        if (playerNameLabel != null) {
            playerNameLabel.toFront();
        }
    }

    boolean animationFlag = true;

    public void setAnimationFlag(boolean animationFlag) {
        this.animationFlag = animationFlag;
    }

    public List<Card> getCards() {
        return cards;
    }

    public void removeCard(Card card) {
        card.setOnMouseEntered(null);
        card.setOnMouseExited(null);
        card.setOnMousePressed(null);
        card.setCursor(Cursor.DEFAULT);

        cards.remove(card);
        getChildren().remove(card);

        rearrange();
    }

    public void changeColorForTurn(boolean turn) {
        log.info("----------------------------------------------------Changing Color");
        if (turn) {
            playerNameLabel.setBorder(new Border(
                    new BorderStroke(
                            Color.WHITE,
                            BorderStrokeStyle.SOLID,
                            new CornerRadii(3),
                            new BorderWidths(2)
                    )
            ));
        }
        else {
            playerNameLabel.setBorder(null);
        }
    }
}
