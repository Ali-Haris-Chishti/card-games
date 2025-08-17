package com.ahccode.common.context;

import com.ahccode.common.card.Card;
import com.ahccode.common.game.GameType;
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


    public static int turn = 0;


    public static boolean GAME_FINISHED = false;



}
