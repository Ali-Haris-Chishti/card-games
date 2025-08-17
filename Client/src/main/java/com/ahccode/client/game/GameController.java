package com.ahccode.client.game;

import com.ahccode.client.gui.controller.ScreenController;
import com.ahccode.client.gui.daketi.DaketiScreen;
import com.ahccode.common.card.Card;
import com.ahccode.common.context.GameContextCore;
import javafx.application.Platform;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class GameController {

    @Getter
    protected int turn;

    protected ScreenController screenController;

    public abstract void startGame(Game game);

    public abstract boolean cardSelected(Card card, int playerNumber);

    public boolean isTurnOfPlayer(int playerNumber) {
        if (playerNumber == -1 || turn != playerNumber) {
            log.warn("Player {} clicked, but it is Player {}'s turn", playerNumber, turn);
            return false;
        }
        return true;
    }

    public void updateTurn() {
        int prevTurn = turn;
        turn = (turn + 1) % 4;
        GameContextCore.turn = turn;
        log.info("Updating Turn: {} to {}", prevTurn, turn);
        Platform.runLater(() -> {
            log.info("Animating turn transition");
            if (screenController.getGameScreen() instanceof DaketiScreen) {
                log.info("Animating turn transition for daketti screen");
                ((DaketiScreen) screenController.getGameScreen()).animateTurnTransition(prevTurn, turn);
            }
            screenController.changeColorForTurn(turn);
        });
    }

    public abstract void clear();

}
