package com.ahccode.client.gui;

import com.ahccode.client.game.Game;
import com.ahccode.common.network.PlayerInfo;
import javafx.scene.layout.Pane;

import java.util.List;

public abstract class GameScreen extends Pane {

    public abstract void initialize(Game game, List<PlayerInfo> playerInfoList);


}
