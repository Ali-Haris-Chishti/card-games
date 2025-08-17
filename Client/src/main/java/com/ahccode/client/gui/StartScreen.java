package com.ahccode.client.gui;

import com.ahccode.client.gui.controller.GameScreenStarter;
import com.ahccode.common.context.GameContextCore;
import com.ahccode.common.context.GameContextUI;
import com.ahccode.common.game.GameType;
import com.ahccode.common.network.CardMessage;
import com.ahccode.common.network.PlayerInfo;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class StartScreen extends Pane {

    private static StartScreen instance;

    private Stage stage;
    Scene scene;
    GameScreenStarter gameScreenStarter;


    private StartScreen(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;

        setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("start-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );

        GameContextCore.setGameType(GameType.DAKETI);

        showGameScreen();
    }

    public static StartScreen getInstance(Stage stage, Scene scene) {
        if (instance == null) {
            instance = new StartScreen(stage, scene);
        }
        return instance;
    }

    public static StartScreen getInstance() {
        if (instance == null) {
            throw new IllegalStateException("StartScreen has not been initialized");
        }
        return instance;
    }


    protected void showGameScreen() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater( () -> {
                    gameScreenStarter = new GameScreenStarter(scene);
                    scene.setRoot(gameScreenStarter.getScreenController().getGameScreen());
                    stage.setFullScreen(true);
                    stage.show();
                });
            }
        }, 100);
    }

    public void startGame(List<PlayerInfo> playerInfoList, List<CardMessage> cardMessages) {
        gameScreenStarter.startGame(playerInfoList, cardMessages);
    }

    public GameScreenStarter getGameScreenStarter() {
        return gameScreenStarter;
    }

    public static void clear() {
        if (instance != null) {
            instance.gameScreenStarter.clear();
            instance.gameScreenStarter = null;
        }
        instance = null;
    }

}