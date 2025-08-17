package com.ahccode.common.network;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@ToString
public class PlayerInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int playerNumber;
    private final String name;

    public PlayerInfo(int playerNumber, String name) {
        this.playerNumber = playerNumber;
        this.name = name;
    }

}
