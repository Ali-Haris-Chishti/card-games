package com.ahccode.cards.ui;

import com.ahccode.cards.card.game.Game;
import javafx.scene.layout.Pane;

public abstract class GameScreen extends Pane {

    public abstract void initialize(Game game);

}
