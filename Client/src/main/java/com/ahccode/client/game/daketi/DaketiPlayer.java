package com.ahccode.client.game.daketi;

import com.ahccode.client.game.Player;
import com.ahccode.client.network.GameClient;
import com.ahccode.common.card.Card;

public class DaketiPlayer extends Player {

    public DaketiPlayer(int playerNumber, String playerName, GameClient associatedClient) {
        super(playerNumber, playerName, associatedClient);
    }


    void addCardInHand(Card card) {
        cardsInHand.add(card);
    }

    @Override
    public String toString() {
        return String.format("Player %s, %S: [cardsInHand=%s]", playerNumber, name, cardsInHand);
    }

}
