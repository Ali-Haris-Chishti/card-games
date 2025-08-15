package com.ahccode.cards.card.game.thulla;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.GameController;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.network.message.MoveMessage;
import com.ahccode.cards.ui.controller.ThullaScreenController;

import java.util.*;

public class ThullaController extends GameController {

    private Thulla thulla;

    boolean firstWave = true;

    private int playerNumberOfHishestCard = 0;

    private static ThullaController instance;
    public static ThullaController getInstance() {
        if (instance == null) {
            instance = new ThullaController();
        }
        return instance;
    }

    private ThullaController() {

    }

    Card previousHighestCard;
    int previousHighestCardPlayerNumber;

    @Override
    public void startGame(Game game) {
        thulla = (Thulla) game;
        turn = thulla.startingPlayer;
        playerNumberOfHishestCard = turn;
        screenController = ThullaScreenController.getInstance(null, null);
        startAceOfSpadeMoveTimer();
    }

    public void startAceOfSpadeMoveTimer() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                firstAceOfSpadeMove();
            }
        }, 5000);
    }

    private void firstAceOfSpadeMove() {
        Card aceOfSpade = thulla.getAceOfSPade(turn);
        thulla.thullaPlayers[turn].cardsInHand.remove(aceOfSpade);
        thulla.cardsInCenter.add(aceOfSpade);
        screenController.applyMoveCardFromHandToCenterAnimation(aceOfSpade, turn);
        previousHighestCard = aceOfSpade;
        previousHighestCardPlayerNumber = turn;
        updateTurn();
    }

    @Override
    public boolean cardSelected(Card card, int playerNumber) {
        if (playerNumber == -1 || turn != playerNumber) {
            System.out.println("Not Player " + playerNumber + "'s turn");
            System.out.println(playerNumber);
            return false;
        }
        System.out.println("Player selected card: " + card);

        if (isFirstCardOfTheWave(card)) {
            previousHighestCard = card;
            previousHighestCardPlayerNumber = turn;
            return true;
        }

        return checkCardToBeAdded(card);
    }

    private boolean isFirstCardOfTheWave(Card card) {
        if (thulla.cardsInCenter.isEmpty()) {
            thulla.thullaPlayers[turn].cardsInHand.remove(card);
            thulla.cardsInCenter.add(card);
            screenController.applyMoveCardFromHandToCenterAnimation(card, turn);
            updateTurn();
            return true;
        }
        return false;
    }

    private boolean checkCardToBeAdded(Card card) {
        CardFamily current = thulla.cardsInCenter.getLast().getCardFamily();

        if (checkForThulla(current, card)) {
            return true;
        }

        if (card.getCardFamily() == current) {
            thulla.thullaPlayers[turn].cardsInHand.remove(card);
            thulla.cardsInCenter.add(card);
            screenController.applyMoveCardFromHandToCenterAnimation(card, turn);
            updateTurnBasedOnHighestCard(card);
            checkForWaveCompletion();
            return true;
        }
        return false;
    }

    private void checkForWaveCompletion() {
        if (thulla.cardsInCenter.size() == thulla.currentPlayersInGame) {
            List<Card> cardsInCenter = thulla.getCardsInCenter();
            thulla.cardsInCenter.clear();
            ((ThullaScreenController) screenController).removeCardsFromCenter(cardsInCenter);
            firstWave = false;
            turn = playerNumberOfHishestCard;
        }
        else
            updateTurn();
    }

    private boolean checkForThulla(CardFamily current, Card selectedCard) {
        if (thulla.getNumberOfCardsOfFamilyOfSinglePlayer(turn, current) == 0) {

            if (firstWave) {
                thulla.thullaPlayers[turn].cardsInHand.remove(selectedCard);
                thulla.cardsInCenter.add(selectedCard);
                screenController.applyMoveCardFromHandToCenterAnimation(selectedCard, turn);
                return false;
            }

            thulla.cardsInCenter.add(selectedCard);
            screenController.applyMoveCardFromHandToCenterAnimation(selectedCard, turn);
            List<Card> cardsInCenter = thulla.getCardsInCenter();
            thulla.cardsInCenter.clear();
            thulla.thullaPlayers[turn].cardsInHand.addAll(cardsInCenter);
            for (Card card : cardsInCenter) {
//                screenController.applyMoveCardFromCenterToHandAnimation(card, playerNumberOfHishestCard);
                GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 0, 3, true));
            }
            turn = playerNumberOfHishestCard;
        }
        return false;
    }

    private void updateTurnBasedOnHighestCard(Card card) {
        if (card.getCardNumber().getThulaOrder() > previousHighestCard.getCardNumber().getThulaOrder()) {
            playerNumberOfHishestCard = turn;
            previousHighestCard = card;
        }
    }

    public void clear() {
        if (instance != null) {

        }
        instance = null;
    }

}
