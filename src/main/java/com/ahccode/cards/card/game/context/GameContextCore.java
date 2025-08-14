package com.ahccode.cards.card.game.context;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.GameType;
import com.ahccode.cards.card.game.Player;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameContextCore {

    public static Card[] deck;

    public static Card getCard(int n) {
        log.info("Retrieving Card: {}", deck[n]);
        return deck[n];
    }


    public static GameType getGameType() {
        return gameType;
    }

    public static void setGameType(GameType gameType) {
        GameContextCore.gameType = gameType;
    }

    private static GameType gameType;


    public static void printDeck() {
        for (Card card : deck) {
            System.out.println(card);
        }
    }

    public static Player currentPlayer;
    public static int currentPlayerNumber;

}
