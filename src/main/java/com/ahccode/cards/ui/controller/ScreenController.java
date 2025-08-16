package com.ahccode.cards.ui.controller;


import com.ahccode.cards.ClientMainFX;
import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.GameController;
import com.ahccode.cards.card.game.GameType;
import com.ahccode.cards.card.game.daketi.DaketiController;
import com.ahccode.cards.card.game.thulla.ThullaController;
import com.ahccode.cards.network.message.CardMessage;
import com.ahccode.cards.network.message.Message;
import com.ahccode.cards.network.message.MessageType;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.component.CardInStackBox;
import com.ahccode.cards.ui.daketi.DaketiScreen;
import com.ahccode.cards.ui.thulla.ThullaScreen;
import javafx.application.Platform;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public abstract class ScreenController {



    protected ScreenController(Game game, Pane gameScreen) {
        this.game = game;
        this.gameScreen = gameScreen;
    }


    public Pane getGameScreen() {
        return gameScreen;
    }

    public void setGameScreen(Pane gameScreen) {
        this.gameScreen = gameScreen;
    }

    protected Pane gameScreen;

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    protected Game game;

    protected Stage stage = ClientMainFX.getPrimaryStage();

    @Getter
    protected GameController gameController;

    protected GameController assignControllerBasedOnGameType() {
        return switch (GameContextCore.getGameType()) {
            case DAKETI -> DaketiController.getInstance();
            case THULLA -> ThullaController.getInstance();
        };
    }

    public void startGame() {
        this.gameController = assignControllerBasedOnGameType();
        gameController.startGame(game);
    }

    public void cardClicked(Card card, int playerNum) {
        if (gameController.cardSelected(card, playerNum)) {
            try {
                log.info("SENDING MOVE MESSAGE");
                GameContextCore.currentPlayer.getAssociatedClient().send(new Message(MessageType.MOVE_CARD, new CardMessage(card.getCardFamily(), card.getCardNumber(), GameContextCore.currentPlayer.getPlayerNumber())));
            } catch (IOException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }



    public int applyMoveCardFromHandToCenterAnimation(Card card, int from) {
        log.info("Apply Move Card From Hand To Center Animation");
        CardInHandBox sourceBox = getHandBoxFromIndex(from);
        CardInHandBox targetBox = getHandBoxFromIndex(4);

        Platform.runLater( () -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
            sourceBox.setAnimationFlag(true);
        });
        return 1;
    }

    public int applyMoveCardFromHandToStackAnimation(Card card, int from, int to) {
        log.info("Apply Move Card From Hand To Stack Animation");
        CardInHandBox sourceBox = getHandBoxFromIndex(from);
        CardInStackBox targetBox = getStackBoxFromIndex(to);

        Platform.runLater( () -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
        });
        return 2;
    }

    public int applyMoveCardFromCenterToHandAnimation(Card card, int to) {
        log.info("Apply Move Card From Center To Hand Animation");
        CardInHandBox sourceBox = getHandBoxFromIndex(4);
        CardInHandBox targetBox = getHandBoxFromIndex(to);

        Platform.runLater( () -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
        });
        return 3;
    }

    public int applyMoveCardFromCenterToStackAnimation(Card card, int to) {
        log.info("Apply Move Card From Center To Stack Animation");
        CardInHandBox sourceBox = getHandBoxFromIndex(4);
        CardInStackBox targetBox = getStackBoxFromIndex(to);

        Platform.runLater( () -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
        });
        return 4;
    }

    public int applyMoveCardFromStackToStackAnimation(Card card, int from, int to) {
        log.info("Apply Move Card From Stack To Stack Animation");
        CardInStackBox sourceBox = getStackBoxFromIndex(from);
        CardInStackBox targetBox = getStackBoxFromIndex(to);

        Platform.runLater( () -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
        });
        return 5;
    }

    public int applyPickNextCardAnimation(Card card, int to) {
        log.info("Apply Pick Next Card Animation");
        CardInStackBox sourceBox = ((DaketiScreen) gameScreen).getStackCardBoxFromIndex(2);
        CardInHandBox targetBox = ((DaketiScreen) gameScreen).getHandCardBoxFromIndex(to);

        Platform.runLater(() -> {
            sourceBox.removeCard(card);
            targetBox.addCard(card);
        });
        return 6;
    }

    enum BoxType {
        HAND, STACK
    }

//    public void moveBasedOnMoveNumber(MoveMessage m) {
//        log.info("Animating:>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
//        log.info("MoveMessage: {}", m);
//        Card card = game.getCardByFamilyAndNumber(m.family, m.number)
//                .orElseThrow(() -> new IllegalArgumentException("No such card"));
//        card.showCard();
//        switch (m.moveNumber) {
//            case 1 -> {applyMoveCardFromHandToCenterAnimation(card, m.fromIndex);}
//            case 2 -> {applyMoveCardFromHandToStackAnimation(card, m.fromIndex, m.toIndex);}
//            case 3 -> {applyMoveCardFromCenterToHandAnimation(card, m.toIndex);}
//            case 4 -> {applyMoveCardFromCenterToStackAnimation(card, m.toIndex);}
//            case 5 -> {applyMoveCardFromStackToStackAnimation(card, m.fromIndex, m.toIndex);}
//            case 6 -> {applyPickNextCardAnimation(card, m.toIndex);}
//        }
//    }


    private Pane getCardBoxFromIndexAndType(int index, BoxType type) {
        Pane cardBox;
        if (GameContextCore.getGameType() == GameType.DAKETI) {
            if (type == BoxType.HAND)
                cardBox = ((DaketiScreen) gameScreen).getHandCardBoxFromIndex(index);
            else
                cardBox = ((DaketiScreen) gameScreen).getStackCardBoxFromIndex(index);
        }
        else {
            cardBox = ((ThullaScreen) gameScreen).getCardInHandBoxFromIndex(index);
        }
        return cardBox;
    }

    public void animateStacks(int scoreToIncrease, int scoreToDecrease) {
        log.info("Animating: [{}, {}]", scoreToIncrease, scoreToDecrease);
        int playerBoxIndex = GameContextCore.turn % 2 == 0 ? 0 : 1;
        int opponentBoxIndex = GameContextCore.turn % 2 == 0 ? 1 : 0;
        CardInStackBox playerBox = ((DaketiScreen) gameScreen).getStackCardBoxFromIndex(playerBoxIndex);
        CardInStackBox opponentBox = ((DaketiScreen) gameScreen).getStackCardBoxFromIndex(opponentBoxIndex);

        if (scoreToIncrease != 0) {
            playerBox.playScoreAnimation(scoreToIncrease);
        }
        if (scoreToDecrease != 0) {
            opponentBox.playScoreAnimation(-1 * scoreToDecrease);
        }

    }

    protected CardInHandBox getHandBoxFromIndex(int index) {
        return (CardInHandBox) getCardBoxFromIndexAndType(index, BoxType.HAND);
    }

    protected CardInStackBox getStackBoxFromIndex(int index) {
        return (CardInStackBox) getCardBoxFromIndexAndType(index, BoxType.STACK);
    }

    public void changeColorForTurn(int turn) {
    }

    public abstract void clear();

}
