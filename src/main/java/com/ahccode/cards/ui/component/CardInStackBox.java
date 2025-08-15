package com.ahccode.cards.ui.component;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Team;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;

public class CardInStackBox extends CardBox {


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

        cards.remove(card);
        getChildren().remove(card);
        repaint();
    }

}
