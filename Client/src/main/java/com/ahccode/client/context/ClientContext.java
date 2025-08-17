package com.ahccode.client.context;

import com.ahccode.client.game.Player;
import com.ahccode.common.network.GameInfo;

public class ClientContext {

    // default for testing purposes
    public static GameInfo gameInfo = new GameInfo("haris", "localhost", 5000);

    public static Player currentPlayer;

}
