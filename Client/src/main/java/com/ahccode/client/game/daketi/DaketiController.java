package com.ahccode.client.game.daketi;

import com.ahccode.client.context.ClientContext;
import com.ahccode.client.gui.controller.DaketiScreenController;
import com.ahccode.common.card.*;
import com.ahccode.client.game.Game;
import com.ahccode.client.game.GameController;
import com.ahccode.common.context.GameContextCore;
import com.ahccode.common.network.CardMessage;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
public class DaketiController extends GameController {

    Daketi daketi;

    boolean cardPickedFlag = false;

    Timer cardPickTimer = new Timer();

    private DaketiController() {
        turn = 0;
    }
    private static DaketiController instance;
    public static DaketiController getInstance() {
        if (instance == null) {
            instance = new DaketiController();
        }
        return instance;
    }

    Runnable animationRunnable = new Runnable() {
        @Override
        public void run() {
            log.info("Animation Runnable Calling");
            cardSelected(selectedCard, turn, false);
        }
    };

    public static void setSelectedCard(CardMessage m) {
        selectedCard = instance.screenController.getGame().getCardByFamilyAndNumber(m.family, m.number)
                .orElseThrow(() -> new IllegalArgumentException("No such card"));
    }

    public static Card selectedCard;

    @Override
    public void startGame(Game game) {
        ClientContext.currentPlayer.getAssociatedClient().setAnimationRunnable(animationRunnable);
        daketi = (Daketi) game;
        screenController = DaketiScreenController.getInstance(null, null);
        startPickCardTimer();
    }

    public boolean cardStackFinished = false;

    public void startPickCardTimer() {
        cardPickedFlag = false;
        cardPickTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                pickNextCard();
            }
        }, 500);
    }


    public boolean cardSelected(Card card, int playerNumber, boolean check) {
        int prevTurn = turn; // variable to store turn of initial player before checking, so if turn is updated, the stack animation is played for correct team
        if (check) {
            if (!cardPickedFlag) {
                log.info("Wait for card to be picked");
                return false;
            }
            if (!isTurnOfPlayer(playerNumber))
                return false;
            log.info("Player selected card: {}", card);
        }
        daketi.daketiPlayers[turn].cardsInHand.remove(card);
        if (!checkCardMove(card)) {

            log.info("No Match found! Turn updated to player {}", playerNumber);
            daketi.cardsInCenter.add(card);

            screenController.applyMoveCardFromHandToCenterAnimation(card, turn);
            if (!cardStackFinished)
                updateTurn();
        }

        if (!cardStackFinished) {
            log.info("Card Stack Not Finished");
            startPickCardTimer();
        }
        else {
            if (daketi.allCardsFinished()) {
                ((DaketiScreenController) screenController).showResults(daketi.getCardStackA(), daketi.getCardStackB());
            }
            else
                updateTurn();
        }

        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> screenController.animateStacks(scoreToIncrease + scoreToDecrease, scoreToDecrease, prevTurn));
            }
        }, 200);
        return check;
    }

    @Override
    public boolean cardSelected(Card card, int playerNumber) {
        return cardSelected(card, playerNumber, true);
    }

    private int scoreToIncrease = 0, scoreToDecrease = 0;

    private boolean checkCardMove(Card card) {
        CardNumber number = card.getCardNumber();
        boolean teamATurn = (turn % 2) == 0;
        boolean match = false;

        scoreToIncrease = 0;

        match = checkMoveFromStacks(card);

        Iterator iterator = daketi.cardsInCenter.iterator();
        while (iterator.hasNext()) {
            Card c = (Card) iterator.next();
            if (c.getCardNumber().equals(number)) {
                scoreToIncrease += c.getCardNumber().getDakettiScore();
                if (teamATurn) {
                    daketi.cardStackA.add(c);
                    screenController.applyMoveCardFromCenterToStackAnimation(c, 0);
                }
                else {
                    daketi.cardStackB.add(c);
                    screenController.applyMoveCardFromCenterToStackAnimation(c, 1);
                }
                match = true;
                iterator.remove();
            }
        }

        if (match) {
            scoreToIncrease += card.getCardNumber().getDakettiScore();
            if (teamATurn) {
                daketi.cardStackA.add(card);
                screenController.applyMoveCardFromHandToStackAnimation(card, turn, 0);
            }
            else {
                daketi.cardStackB.add(card);
                screenController.applyMoveCardFromHandToStackAnimation(card, turn, 1);
            }
        }

        return match;
    }

    private boolean checkMoveFromStacks(Card card) {
        boolean teamATurn = (turn % 2) == 0;

        boolean match = false;
        boolean matchFromOppositeStack = true;
        scoreToDecrease = 0;

        if (teamATurn) {
            if (!daketi.cardStackA.isEmpty() && daketi.cardStackA.peek().getCardNumber().equals(card.getCardNumber())) {
                match = true;
            }
        }
        else {
            if (!daketi.cardStackB.isEmpty() && daketi.cardStackB.peek().getCardNumber().equals(card.getCardNumber())) {
                match = true;
            }
        }

        while (matchFromOppositeStack) {
            matchFromOppositeStack = false;
            if (teamATurn) {
                if (!daketi.cardStackB.isEmpty() && daketi.cardStackB.peek().getCardNumber().equals(card.getCardNumber())) {
                    Card cardFromOppositeStack = daketi.cardStackB.pop();
                    daketi.cardStackA.add(cardFromOppositeStack);
                    screenController.applyMoveCardFromStackToStackAnimation(cardFromOppositeStack, 1, 0);
                    card.showCard(true);
                    match = matchFromOppositeStack = true;
                    scoreToDecrease += card.getCardNumber().getDakettiScore();
                }
            }
            else {
                if (!daketi.cardStackA.isEmpty() && daketi.cardStackA.peek().getCardNumber().equals(card.getCardNumber())) {
                    Card cardFromOppositeStack = daketi.cardStackA.pop();
                    daketi.cardStackB.add(cardFromOppositeStack);
                    screenController.applyMoveCardFromStackToStackAnimation(cardFromOppositeStack, 0, 1);
                    card.showCard(true);
                    match = matchFromOppositeStack = true;
                    scoreToDecrease += card.getCardNumber().getDakettiScore();
                }
            }
        }

        card.showCard(true);

        return match;
    }


    public void pickNextCard() {
        if (!daketi.remainingCards.isEmpty()){
            Card card = daketi.remainingCards.pop();
            daketi.daketiPlayers[turn].cardsInHand.add(card);
            card.showCard(turn == ClientContext.currentPlayer.getPlayerNumber());
            screenController.applyPickNextCardAnimation(card, turn);
        }
        else {
            log.info("Cards to pick stack finished");
            cardStackFinished = true;
        }
        cardPickedFlag = true;
    }

    public void clear() {
        if (instance != null) {
            if (cardPickTimer != null)
                cardPickTimer.cancel();
        }
        instance = null;
    }

}
