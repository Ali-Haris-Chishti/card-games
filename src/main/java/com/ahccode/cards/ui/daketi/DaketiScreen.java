package com.ahccode.cards.ui.daketi;

import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.PlayerInfo;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.ui.GameScreen;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.component.CardInStackBox;
import javafx.animation.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.QuadCurveTo;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.ahccode.cards.card.game.Team.*;

@Slf4j
public class DaketiScreen extends GameScreen {

    private Daketi daketi;

    List<CardInHandBox> handCardBoxes;
    List<CardInStackBox> stackCardBoxes;

    // Turn animation elements
    private Circle turnIndicator;
    private int currentTurnPlayer = -1;

    public CardInHandBox getHandCardBoxFromIndex(int index) {
        return handCardBoxes.get(index);
    }

    public CardInStackBox getStackCardBoxFromIndex(int index) {
        return stackCardBoxes.get(index);
    }

    public DaketiScreen() {}

    @Override
    public void initialize(Game game, List<PlayerInfo> playerInfoList) {
        log.info("Players Info: {}", playerInfoList);
        this.daketi = (Daketi) game;
        handCardBoxes = new ArrayList<>();
        stackCardBoxes = new ArrayList<>();

        Team[] teams = {TEAM_GREEN, TEAM_BLUE};

        // Create hand card boxes with player names
        for (int i = 0; i < 4; i++) {
            String playerName = playerInfoList.get(i).getName();
            handCardBoxes.add(new CardInHandBox(daketi.getCardsOfPlayer(i), teams[i % 2], i, playerName));
            getChildren().add(handCardBoxes.get(i));
        }

        // Create stack boxes
        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackA(), TEAM_GREEN, -1));
        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackB(), TEAM_BLUE, -1));
        stackCardBoxes.add(new CardInStackBox(daketi.getRemainingCards(), NONE, -1));

        for (CardInStackBox stackBox : stackCardBoxes)
            getChildren().add(stackBox);

        // Center cards box (no player name needed - pass null)
        handCardBoxes.add(new CardInHandBox(daketi.getCardsInCenter(), NONE, -1, null));
        getChildren().add(handCardBoxes.getLast());

        // Initialize turn indicator
        initializeTurnIndicator();

        // Position on layout pass
        layoutBoundsProperty().addListener((obs, oldVal, newVal) -> layoutChildren());
    }

    private void initializeTurnIndicator() {
        turnIndicator = new Circle(15);
        turnIndicator.setFill(Color.GOLD);
        turnIndicator.setStroke(Color.ORANGE);
        turnIndicator.setStrokeWidth(2);
        turnIndicator.setOpacity(0); // hidden until used
        getChildren().add(turnIndicator);
    }

    /**
     * Animates turn transition from one player to another
     */
    private void animateTurnIndicatorMovement(int fromPlayer, int toPlayer) {
        double[] fromCenter = getPlayerBoxCenter(fromPlayer);
        double[] toCenter = getPlayerBoxCenter(toPlayer);

        // Start position
        turnIndicator.setCenterX(fromCenter[0]);
        turnIndicator.setCenterY(fromCenter[1]);

        // Make visible only during movement
        turnIndicator.setOpacity(1);

        Path path = createMovementPath(fromCenter, toCenter);

        PathTransition pathTransition = new PathTransition();
        pathTransition.setDuration(Duration.millis(800));
        pathTransition.setPath(path);
        pathTransition.setNode(turnIndicator);
        pathTransition.setInterpolator(Interpolator.EASE_BOTH);

        // Hide when finished
        pathTransition.setOnFinished(e -> turnIndicator.setOpacity(0));

        pathTransition.play();
    }

    public void animateTurnTransition(int previousTurnPlayer, int nextTurnPlayer) {
        if (nextTurnPlayer < 0 || nextTurnPlayer > 3) {
            log.warn("Invalid next turn player index: {}", nextTurnPlayer);
            return;
        }

        currentTurnPlayer = nextTurnPlayer;

        if (previousTurnPlayer == -1) {
            // First turn: do not show circle, just skip
            return;
        }

        animateTurnIndicatorMovement(previousTurnPlayer, nextTurnPlayer);
    }


    private Path createMovementPath(double[] from, double[] to) {
        Path path = new Path();
        MoveTo start = new MoveTo(from[0], from[1]);

        double midX = (from[0] + to[0]) / 2;
        double midY = (from[1] + to[1]) / 2;

        double centerX = getWidth() / 2;
        double centerY = getHeight() / 2;

        double controlX = midX + (centerX - midX) * 0.3;
        double controlY = midY + (centerY - midY) * 0.3;

        QuadCurveTo curve = new QuadCurveTo(controlX, controlY, to[0], to[1]);
        path.getElements().addAll(start, curve);
        return path;
    }

    private double[] getPlayerBoxCenter(int playerIndex) {
        CardInHandBox box = handCardBoxes.get(playerIndex);
        double centerX = box.getLayoutX() + box.getWidth() / 2;
        double centerY = box.getLayoutY() + box.getHeight() / 2;
        return new double[]{centerX, centerY};
    }

    public void cleanupTurnAnimations() {
        turnIndicator.setOpacity(0);
        currentTurnPlayer = -1;
    }

    @Override
    protected void layoutChildren() {
        double padding = 20;
        double spacing = 10;

        double width = getWidth();
        double height = getHeight();

        // Hand boxes
        CardInHandBox topLeftBox = handCardBoxes.get(0);
        CardInHandBox topRightBox = handCardBoxes.get(1);
        CardInHandBox bottomRightBox = handCardBoxes.get(2);
        CardInHandBox bottomLeftBox = handCardBoxes.get(3);
        CardInHandBox centerHandBox = handCardBoxes.get(4);

        double tlw = topLeftBox.prefWidth(-1), tlh = topLeftBox.prefHeight(-1);
        double trw = topRightBox.prefWidth(-1), trh = topRightBox.prefHeight(-1);
        double brw = bottomRightBox.prefWidth(-1), brh = bottomRightBox.prefHeight(-1);
        double blw = bottomLeftBox.prefWidth(-1), blh = bottomLeftBox.prefHeight(-1);
        double chw = centerHandBox.prefWidth(-1), chh = centerHandBox.prefHeight(-1);

        // Stack boxes
        CardInStackBox stackABox = stackCardBoxes.get(0);
        CardInStackBox stackBBox = stackCardBoxes.get(1);
        CardInStackBox remainingBox = stackCardBoxes.get(2);

        double saw = stackABox.prefWidth(-1), sah = stackABox.prefHeight(-1);
        double sbw = stackBBox.prefWidth(-1), sbh = stackBBox.prefHeight(-1);
        double rw = remainingBox.prefWidth(-1), rh = remainingBox.prefHeight(-1);

        // Position hand boxes
        topLeftBox.resizeRelocate(padding, padding, tlw, tlh);
        topRightBox.resizeRelocate(width - trw - padding, padding, trw, trh);
        bottomRightBox.resizeRelocate(width - brw - padding, height - brh - padding, brw, brh);
        bottomLeftBox.resizeRelocate(padding, height - blh - padding, blw, blh);

        // Stack A: Top center
        stackABox.resizeRelocate((width - saw) / 2, padding, saw, sah);

        // Stack B: Bottom center
        stackBBox.resizeRelocate((width - sbw) / 2, height - sbh - padding, sbw, sbh);

        // Remaining + center cards
        double totalCenterWidth = rw + spacing + chw;
        double centerX = (width - totalCenterWidth) / 2;
        double centerY = (height - Math.max(rh, chh)) / 2;

        remainingBox.resizeRelocate(centerX, centerY, rw, rh);
        centerHandBox.resizeRelocate(centerX + rw + spacing, centerY, chw, chh);
    }
}
