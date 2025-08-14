package com.ahccode.cards.card.game.thulla;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.network.GameClient;

import java.util.ArrayList;
import java.util.List;

public class ThullaPlayer extends Player {


    ThullaPlayer(int playerNumber, String name, GameClient associatedClient) {
        super(playerNumber, name, associatedClient);
    }

    @Override
    public String toString() {
        return String.format("Player %s: [cardsInHand=%s]", playerNumber, cardsInHand);
    }

}
