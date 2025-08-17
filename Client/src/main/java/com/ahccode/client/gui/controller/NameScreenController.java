package com.ahccode.client.gui.controller;

import com.ahccode.client.game.Game;
import javafx.scene.layout.Pane;

public class NameScreenController extends ScreenController{

    private static NameScreenController instance;

    private NameScreenController(Game game, Pane gameScreen) {
        super(game, gameScreen);
    }

    @Override
    public void clear() {

    }

    public static NameScreenController getInstance(Game game, Pane gameScreen) {
        if (instance == null) {
            instance = new NameScreenController(game, gameScreen);
        }
        return instance;
    }



}
