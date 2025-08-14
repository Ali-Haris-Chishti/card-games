package com.ahccode.cards.ui.component;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.context.GameContextCore;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class CardInHandBox extends CardBox {

    public CardInHandBox(List<Card> cards, Team team, int playerNumber) {
        super(cards, team, playerNumber, 30);

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

        log.info("Cards In Hand: {}", cards);
        // Build the cards
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            card.setFitWidth(CARD_WIDTH);
            card.setFitHeight(CARD_HEIGHT);

            // Center the whole group by offsetting initial layoutX
            double layoutX = (getPrefWidth() - totalWidth) / 2 + i * CARD_OVERLAP;
            card.setLayoutX(layoutX);
            card.setLayoutY(containerPadding);

            // Hover effects
            card.setOnMouseEntered(e -> handleMouseEnter(card));
            card.setOnMouseExited(e -> handleMouseExit(card, layoutX));

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
    }

    public void rearrange() {

        animationFlag = false;

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

            // Center the whole group by offsetting initial layoutX
            double layoutX = (getPrefWidth() - totalWidth) / 2 + i * CARD_OVERLAP;
            card.setLayoutX(layoutX);
            card.setLayoutY(containerPadding);

            // Hover effects
            card.setOnMouseEntered(e -> handleMouseEnter(card));
            card.setOnMouseExited(e -> handleMouseExit(card, layoutX));

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
    }

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

    private void handleMouseEnter(ImageView cardView) {
        if (!animationFlag)
            return;
        cardView.toFront();
        cardView.setLayoutY(cardView.getLayoutY() - 10); // raise it a bit
        cardView.setScaleX(1.07);
        cardView.setScaleY(1.07);
        cardView.setEffect(new DropShadow(20, Color.BLACK));
    }

    private void handleMouseExit(ImageView cardView, double originalX) {
        if (!animationFlag)
            return;
        cardView.setLayoutY(getPadding().getTop());
        cardView.setScaleX(1.0);
        cardView.setScaleY(1.0);
        DropShadow defaultShadow = new DropShadow(8, Color.gray(0.4));
        cardView.setEffect(defaultShadow);
        setInitialPattern();
    }

    private void setInitialPattern() {
        for (Card card : cards) {
            card.toFront();
        }
    }

    boolean animationFlag = true;

    public void setAnimationFlag(boolean animationFlag) {
        this.animationFlag = animationFlag;
    }

    public void addCard(Card card) {

        // Add to list
        cards.add(card);
        getChildren().add(card);

        // Get current layout size
        double totalWidth = CARD_WIDTH + (cards.size() - 1) * CARD_OVERLAP;
        setPrefWidth(totalWidth + 40); // 40 for padding on both sides

        // Animate card from current position to its new one
        double targetX = (getPrefWidth() - totalWidth) / 2 + (cards.size() - 1) * CARD_OVERLAP;
        double targetY = getPadding().getTop();

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

    public void changeColorForTurn() {
        log.info("----------------------------------------------------Changing Color");
        Color teamColor = Color.rgb(250, 250, 0, 0.4);
        setBackground(new Background(new BackgroundFill(teamColor, new CornerRadii(15), Insets.EMPTY)));
        setBorder(new Border(new BorderStroke(teamColor.darker(), BorderStrokeStyle.SOLID, new CornerRadii(15), new BorderWidths(2))));
    }

}
