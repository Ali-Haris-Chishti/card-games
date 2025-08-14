package com.ahccode.cards.ui.controller;

import com.ahccode.cards.card.game.Game;
import javafx.scene.layout.Pane;

public class NameScreenController extends ScreenController{

    private static NameScreenController instance;

    private NameScreenController(Game game, Pane gameScreen) {
        super(game, gameScreen);
    }

    public static NameScreenController getInstance(Game game, Pane gameScreen) {
        if (instance == null) {
            instance = new NameScreenController(game, gameScreen);
        }
        return instance;
    }



}
