package com.ahccode.client.game.thulla;

import com.ahccode.client.game.Player;
import com.ahccode.client.network.GameClient;

public class ThullaPlayer extends Player {


    ThullaPlayer(int playerNumber, String name, GameClient associatedClient) {
        super(playerNumber, name, associatedClient);
    }

    @Override
    public String toString() {
        return String.format("Player %s: [cardsInHand=%s]", playerNumber, cardsInHand);
    }

}
