package com.ahccode.cards.card.game;

import java.io.Serializable;

public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int playerNumber;
    private final String name;

    public PlayerInfo(int playerNumber, String name) {
        this.playerNumber = playerNumber;
        this.name = name;
    }

    public int getPlayerNumber() { return playerNumber; }
    public String getName() { return name; }
}
