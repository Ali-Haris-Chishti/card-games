package com.ahccode.client.gui.controller;

import com.ahccode.client.game.Game;
import com.ahccode.client.gui.component.CardInHandBox;
import com.ahccode.common.card.Card;
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

    @Override
    public void clear() {

    }

    public void removeCardsFromCenter(List<Card> cards) {
        CardInHandBox sourceBox = getHandBoxFromIndex(4);

        Platform.runLater( () -> {
            for (Card card : cards) {
                sourceBox.removeCard(card);
            }
        });
    }


}
