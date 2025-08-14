package com.ahccode.cards.card.game.daketi;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.network.GameClient;
import lombok.ToString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
