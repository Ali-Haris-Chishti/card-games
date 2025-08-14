package com.ahccode.cards.card.game.thulla;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.network.message.CardMessage;

import java.util.*;

public class Thulla extends Game {


    final ThullaPlayer [] thullaPlayers;

    final List<Card> cardsInCenter;

    int currentPlayersInGame = 4;

    boolean [] playersInGameStatus = {true, true, true, true};

    public void playerWon(int playerNumber) {
        playersInGameStatus[playerNumber] = false;
        if (currentPlayersInGame > 0) {
            currentPlayersInGame--;
        }
    }


    public Thulla() {
        thullaPlayers = new ThullaPlayer[4];
        cardsInCenter = new ArrayList<>();
    }


    public void startGame(List<CardMessage> cardMessages, int playerNumber) {
        assignCardsToAllPlayers();
        printInitialDistribution();
    }

    @Override
    public Optional<Card> getCardByFamilyAndNumber(CardFamily family, CardNumber number) {
        return Optional.empty();
    }

    private void assignCardsToAllPlayers() {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < 52; i++) {
            indices.add(i);
        }
        Collections.shuffle(indices);


        int pointer = 0;
        while (pointer < indices.size()) {
            for (int j = 0; j < 4; j++) {
                Card card = GameContextCore.getCard(indices.get(pointer++));
                if (isAceOfSPade(card)) {
                    startingPlayer = j;
                    System.out.println("Player " + j + " has ACE OF SPADE");
                }
                thullaPlayers[j].cardsInHand.add(card);
                if (pointer == 52) break;
            }
        }

        // Sort cards for each player by family and thula order
        for (ThullaPlayer player : thullaPlayers) {
            player.cardsInHand.sort(cardComparator());
        }
    }

    private Comparator<Card> cardComparator() {
        return Comparator
                .comparing(Card::getCardFamily) // First group by family
                .thenComparing(card -> card.getCardNumber().getThulaOrder()); // Then by thulaOrder
    }


    public void printInitialDistribution() {
        for (int i = 0; i < thullaPlayers.length; i++) {
            System.out.println(thullaPlayers[i]);
        }
    }

    public List<Card> getCardsOfPlayerNumber(int n) {
        return new ArrayList<>(thullaPlayers[n].cardsInHand);
    }

    public List<Card> getCardsInCenter() {
        return new ArrayList<>(cardsInCenter);
    }

    int startingPlayer = 0;

    Card getAceOfSPade(int playerNumber) {
        for (Card card : thullaPlayers[playerNumber].cardsInHand) {
            if (card.getCardFamily() == CardFamily.SPADE && card.getCardNumber() == CardNumber.ACE)
                return card;
        }
        return null;
    }

    boolean isAceOfSPade(Card card) {
        return card.getCardFamily() == CardFamily.SPADE && card.getCardNumber() == CardNumber.ACE;
    }

    int getNumberOfCardsOfFamilyOfSinglePlayer(int playerNumber, CardFamily family) {
        return thullaPlayers[playerNumber].cardsInHand.stream().filter(card -> card.getCardFamily() == family).toList().size();
    }

}
