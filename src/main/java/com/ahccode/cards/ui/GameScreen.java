package com.ahccode.cards.ui;

import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.PlayerInfo;
import javafx.scene.layout.Pane;

import java.util.List;

public abstract class GameScreen extends Pane {

    public abstract void initialize(Game game, List<PlayerInfo> playerInfoList);


}
