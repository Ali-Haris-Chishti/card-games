package com.ahccode.cards.card.game.daketi;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.PlayerInfo;
import com.ahccode.cards.network.message.CardMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static com.ahccode.cards.card.game.context.GameContextCore.deck;

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


    public Daketi(List<PlayerInfo> playerInfoList) {
        remainingCards = new Stack<>();
        cardStackA = new Stack<>();
        cardStackB = new Stack<>();

        cardsInCenter = new ArrayList<>();

        daketiPlayers = new DaketiPlayer[4];
        for (int i = 0; i < 4; i++) {
            daketiPlayers[i] = new DaketiPlayer(playerInfoList.get(i).getPlayerNumber(), playerInfoList.get(i).getName(), null);
//            daketiPlayers[i] = new DaketiPlayer(i, "name", null);
        }
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

    public void assignCardsToPlayersAndCenter(CardMessage [] cardMessages) {
        // Create and shuffle a list of indices from 0 to 55

        deck = Arrays.stream(cardMessages)
                .map(CardMessage::toCard)
                .toArray(Card[]::new);
        log.info("Deck: {}", deck);

        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);


        int pointer = 0;


        // Give 4 cards to each player
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                daketiPlayers[i].addCardInHand(GameContextCore.getCard(indices.get(pointer++)));
            }
        }

        // Place 4 cards in center
        for (int i = 0; i < 4; i++) {
            cardsInCenter.add(GameContextCore.getCard(indices.get(pointer++)));
        }

        // Remaining 36 cards
        while (pointer < indices.size()) {
            remainingCards.add(GameContextCore.getCard(indices.get(pointer++)));
        }
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
