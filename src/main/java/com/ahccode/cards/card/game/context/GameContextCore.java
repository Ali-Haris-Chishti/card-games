package com.ahccode.cards.card.game.context;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.GameType;
import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.network.GameInfo;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GameContextCore {

    public static Card[] deck;

    public static Card getCard(int n) {
        log.info("Retrieving Card: {}", deck[n]);
        return deck[n];
    }


    public static void setGameType(GameType gameType) {
        GameContextCore.gameType = gameType;
    }

    @Getter
    private static GameType gameType = GameType.DAKETI;


    public static void printDeck() {
        for (Card card : deck) {
            System.out.println(card);
        }
    }

    public static Player currentPlayer;

    public static int turn = 0;


    public static boolean GAME_FINISHED = false;

    // default for testing purposes
    public static GameInfo gameInfo = new GameInfo("haris", "localhost", 5000);

}
