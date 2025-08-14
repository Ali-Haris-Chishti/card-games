package com.ahccode.cards.ui.controller;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.ui.component.CardInHandBox;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import java.util.List;

public class ThullaScreenController extends ScreenController{

    private static ThullaScreenController instance;


    public static ThullaScreenController getInstance(Game game, Pane gameScreen) {
        if (instance == null) {
            instance = new ThullaScreenController(game, gameScreen);
        }
        return instance;
    }

    private ThullaScreenController(Game game, Pane gameScreen) {
        super(game, gameScreen);
    }

    public void removeCardsFromCenter(List<Card> cards) {
        CardInHandBox sourceBox = getHandBoxFromIndex(4);

        Platform.runLater( () -> {
            for (Card card : cards) {
                sourceBox.removeCard(card);
            }
        });
    }

    public void changeColorForTurn(int turn) {
        CardInHandBox sourceBox = getHandBoxFromIndex(turn);
        Platform.runLater(sourceBox::changeColorForTurn);
    }

}
