package com.ahccode.cards.card.game.daketi;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.GameController;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.network.message.CardMessage;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.network.message.MoveMessage;
import com.ahccode.cards.ui.controller.DaketiScreenController;
import com.ahccode.cards.ui.controller.ScreenController;
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
//            animateBasedOnMove();
            cardSelected(selectedCard, turn, false);
            screenController.changeColorForTurn(turn);
        }
    };

    public static void setSelectedCard(CardMessage m) {
        selectedCard = instance.screenController.getGame().getCardByFamilyAndNumber(m.family, m.number)
                .orElseThrow(() -> new IllegalArgumentException("No such card"));
    }

    public static Card selectedCard;

    @Override
    public void startGame(Game game) {
        GameContextCore.currentPlayer.getAssociatedClient().setAnimationRunnable(animationRunnable);
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
//            GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 0, 1, !cardStackFinished));
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
            updateTurn();
        }

        screenController.changeColorForTurn(turn);
        return check;
    }

    @Override
    public boolean cardSelected(Card card, int playerNumber) {
        return cardSelected(card, playerNumber, true);
    }


    private boolean checkCardMove(Card card) {
        CardNumber number = card.getCardNumber();
        boolean teamATurn = (turn % 2) == 0;
        boolean match = false;

        match = checkMoveFromStacks(card);

        Iterator iterator = daketi.cardsInCenter.iterator();
        while (iterator.hasNext()) {
            Card c = (Card) iterator.next();
            if (c.getCardNumber().equals(number)) {
                if (teamATurn) {
                    daketi.cardStackA.add(c);
                    screenController.applyMoveCardFromCenterToStackAnimation(c, 0);
//                    GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 0, 4, false));
                }
                else {
                    daketi.cardStackB.add(c);
                    screenController.applyMoveCardFromCenterToStackAnimation(c, 1);
//                    GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 1, 4, false));
                }
                match = true;
                iterator.remove();
            }
        }

        if (match) {
            if (teamATurn) {
                daketi.cardStackA.add(card);
                screenController.applyMoveCardFromHandToStackAnimation(card, turn, 0);
//                GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 0, 2, false));
            }
            else {
                daketi.cardStackB.add(card);
                screenController.applyMoveCardFromHandToStackAnimation(card, turn, 1);
//                GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), turn, 1, 2, false));
            }
        }

        return match;
    }

    private boolean checkMoveFromStacks(Card card) {
        boolean teamATurn = (turn % 2) == 0;

        boolean match = false;
        boolean matchFromOppositeStack = true;

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
//                    GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), 1, 0, 5, false));
                    match = matchFromOppositeStack = true;
                }
            }
            else {
                if (!daketi.cardStackA.isEmpty() && daketi.cardStackA.peek().getCardNumber().equals(card.getCardNumber())) {
                    Card cardFromOppositeStack = daketi.cardStackA.pop();
                    daketi.cardStackB.add(cardFromOppositeStack);
                    screenController.applyMoveCardFromStackToStackAnimation(cardFromOppositeStack, 0, 1);
                    card.showCard(true);
//                    GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), 0, 1, 5, false));
                    match = matchFromOppositeStack = true;
                }
            }
        }

        card.showCard(true);

        return match;
    }


    public void pickNextCard() {
        screenController.changeColorForTurn(turn);
        if (!daketi.remainingCards.isEmpty()){
//             && GameContextCore.currentPlayer.getPlayerNumber() == turn
            Card card = daketi.remainingCards.pop();
            daketi.daketiPlayers[turn].cardsInHand.add(card);
            card.showCard(turn == GameContextCore.currentPlayer.getPlayerNumber());
            screenController.applyPickNextCardAnimation(card, turn);
//            GameContextCore.currentPlayer.moveCard(new MoveMessage(card.getCardFamily(), card.getCardNumber(), 0, turn, 6, false));
        }
        else {
            log.error("Not current Player's turn");
        }
        if (daketi.remainingCards.isEmpty())
            cardStackFinished = true;
        cardPickedFlag = true;
    }



    public static MoveMessage currentMessage;
    public static void animateBasedOnMove() {
        log.info("Animate Based on Move Calling");
//        DaketiScreenController.getInstance().moveBasedOnMoveNumber(currentMessage);
        if (currentMessage.updateTurn) {
            instance.updateTurn();
        }
    }

}
