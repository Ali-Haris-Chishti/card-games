package com.ahccode.cards.ui.controller;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.daketi.DaketiGameFinishedScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.ahccode.cards.ui.daketi.DaketiGameFinishedScreen.GameResult.*;

public class DaketiScreenController extends ScreenController {

    private static DaketiScreenController instance;


    public static DaketiScreenController getInstance(Game game, Pane gameScreen) {
        if (instance == null) {
            instance = new DaketiScreenController(game, gameScreen);
        }
        return instance;
    }

    public static DaketiScreenController getInstance() {
        if (instance == null) {
            throw new NullPointerException("DaketiScreenController instance is null");
        }
        return instance;
    }

    private DaketiScreenController(Game game, Pane gameScreen) {
        super(game, gameScreen);
    }

    public void showResults(List<Card> stackA, List<Card> stackB) {
        int scoreA;
        int scoreB;

        scoreA = stackA.stream().mapToInt(card -> card.getCardNumber().getDakettiScore()).sum();
        scoreB = stackB.stream().mapToInt(card -> card.getCardNumber().getDakettiScore()).sum();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater( () -> {
                    Scene scene = new Scene(new DaketiGameFinishedScreen(stackA, stackB, scoreA, scoreB, getResult(scoreA, scoreB)));
                    stage.setScene(scene);
                    stage.setFullScreen(true);
                    stage.show();
                    scene.setOnKeyPressed(e -> {
                        if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                            System.exit(0);
                        }
                    });
                });
            }
        }, 1000);

    }

    private DaketiGameFinishedScreen.GameResult getResult(int scoreA, int scoreB) {
        if (GameContextCore.currentPlayer.getPlayerNumber() % 2 == 0) {
            return scoreA > scoreB? WIN: scoreA == scoreB? DRAW: LOSE;
        }
        else {
            return scoreA > scoreB? LOSE: scoreA == scoreB? DRAW: WIN;
        }
    }

    public void changeColorForTurn(int turn) {
        for (int i = 0; i < 4; i++) {
            CardInHandBox targetBox = getHandBoxFromIndex(i);
            int finalI = i;
            Platform.runLater( () -> {targetBox.changeColorForTurn(turn == finalI);});
        }
    }

    public void clear() {
        if (instance != null) {
            gameController.clear();
        }
        instance = null;
    }

}
