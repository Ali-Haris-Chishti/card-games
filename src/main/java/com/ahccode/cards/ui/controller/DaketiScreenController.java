package com.ahccode.cards.ui.controller;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.daketi.DaketiGameFinishedScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
                    Stage s = new Stage(StageStyle.TRANSPARENT);
                    Scene scene = new Scene(new DaketiGameFinishedScreen(stackA, stackB, scoreA, scoreB));
                    s.setScene(scene);
                    s.setFullScreen(true);
                    s.show();
                });
            }
        }, 1000);

    }

    public void changeColorForTurn(int turn) {
        CardInHandBox sourceBox = getHandBoxFromIndex(turn);
        Platform.runLater(sourceBox::changeColorForTurn);
    }

}
