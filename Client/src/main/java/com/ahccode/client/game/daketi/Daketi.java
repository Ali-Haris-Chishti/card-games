package com.ahccode.client.game.daketi;

import com.ahccode.client.game.Game;
import com.ahccode.common.card.Card;
import com.ahccode.common.card.CardFamily;
import com.ahccode.common.card.CardNumber;
import com.ahccode.common.network.CardMessage;
import com.ahccode.common.network.PlayerInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.*;


@Slf4j
public class Daketi extends Game {

    Stack<Card> remainingCards;
    Stack<Card> cardStackA;
    Stack<Card> cardStackB;

    List<Card> cardsInCenter;

    DaketiPlayer[] daketiPlayers;

    public List<Card> getCardsOfPlayer(int n) {
        return new ArrayList<>(daketiPlayers[n].cardsInHand);
    }

    public List<Card> getCardsInCenter() {
        return new ArrayList<>(cardsInCenter);
    }

    public List<Card> getRemainingCards() {
        return new ArrayList<>(remainingCards); // Copies stack into list
    }

    public List<Card> getCardStackA() {
        return new ArrayList<>(cardStackA);
    }

    public List<Card> getCardStackB() {
        return new ArrayList<>(cardStackB);
    }

    public List<DaketiPlayer> getDaketiPlayers() {
        return Arrays.asList(daketiPlayers.clone()); // clone to protect array
    }


    public Daketi(List<PlayerInfo> playerInfoList, DaketiPlayer player) {
        remainingCards = new Stack<>();
        cardStackA = new Stack<>();
        cardStackB = new Stack<>();

        cardsInCenter = new ArrayList<>();

        daketiPlayers = new DaketiPlayer[4];
        for (int i = 0; i < 4; i++) {
            if (i == player.getPlayerNumber()) {
                daketiPlayers[i] = player;
                continue;
            }
            daketiPlayers[i] = new DaketiPlayer(playerInfoList.get(i).getPlayerNumber(), playerInfoList.get(i).getName(), null);
        }

        for (int i = 0; i < 4; i++) {
            log.info("Players Added: {}", daketiPlayers[i]);
        }
    }

    public void startGame(List<CardMessage> cardMessages, int playerNumber) {
        System.out.println("-----------------------------------------------------------");;
        log.info("Starting Game For Player {}", playerNumber);
        List<Card> cards = cardMessages.stream().map(CardMessage::toCard).toList();
        log.info("Cards: {}", cards);
        for (int i = 0, k = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++, k++) {
                log.info("Adding Card: {}", cards.get(k));
                cards.get(k).showCard(i == playerNumber);
                daketiPlayers[i].cardsInHand.add(cards.get(k));
            }
        }
        for (int i = 16; i < 20; i++) {
            cards.get(i).showCard(true);
            cardsInCenter.add(cards.get(i));
        }
        for (int i = 20; i < 52; i++) {
            remainingCards.add(cards.get(i));
            cards.get(i).showCard(false);
        }
        for (int i = 0; i < 4; i++) {
            System.out.println("===========================================================================");
            System.out.printf("Player %d: %s\n", i, daketiPlayers[i].cardsInHand);
        }
    }

    @Override
    public Optional<Card> getCardByFamilyAndNumber(CardFamily family, CardNumber number) {
        log.info("Finding Card with Family: {}, Number: {}", family, number);
        for (Card card : cardsInCenter) {
            if (card.equals(family, number))
                return Optional.of(card);
        }
        for (Card card : cardStackA) {
            if (card.equals(family, number))
                return Optional.of(card);
        }
        for (Card card : cardStackB) {
            if (card.equals(family, number))
                return Optional.of(card);
        }
        for (int i = 0; i < 4; i++) {
            for (Card card : daketiPlayers[i].cardsInHand) {
                if (card.equals(family, number))
                    return Optional.of(card);
            }
        }
        return Optional.empty();
    }

    public void printInitialDistribution() {
        for (int i = 0; i < daketiPlayers.length; i++) {
            System.out.println(daketiPlayers[i]);
        }

        System.out.println("\nCards in center are: ");
        System.out.println(cardsInCenter);

        System.out.println("\nRemaining cards are: ");
        System.out.println(remainingCards);
    }

    public boolean allCardsFinished() {
        int allCardsInHandCount = 0;
        for (int i = 0; i < daketiPlayers.length; i++) {
            allCardsInHandCount += daketiPlayers[i].cardsInHand.size();
        }
        return allCardsInHandCount == 0;
    }

}
