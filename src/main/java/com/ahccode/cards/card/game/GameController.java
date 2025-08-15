package com.ahccode.cards.card.game;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.ui.controller.ScreenController;
import javafx.application.Platform;
import javafx.scene.paint.Color;
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
        log.info("Updating Turn");
        log.info("From : {}", turn);
        turn = (turn + 1) % 4;
        GameContextCore.turn = turn;
        log.info("To   : {}", turn);
        Platform.runLater(() -> screenController.changeColorForTurn(turn));
    }

}
