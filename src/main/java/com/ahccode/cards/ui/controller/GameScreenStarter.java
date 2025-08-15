package com.ahccode.cards.ui.controller;

import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.card.game.PlayerInfo;
import com.ahccode.cards.card.game.context.GameContextUI;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.card.game.daketi.DaketiPlayer;
import com.ahccode.cards.card.game.thulla.Thulla;
import com.ahccode.cards.network.message.CardMessage;
import com.ahccode.cards.ui.GameScreen;
import com.ahccode.cards.ui.PlayerWaitingScreen;
import com.ahccode.cards.ui.daketi.DaketiScreen;
import com.ahccode.cards.ui.thulla.ThullaScreen;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class GameScreenStarter {


    public void setScreenController(ScreenController screenController) {
        this.screenController = screenController;
    }

    @Getter
    private ScreenController screenController;

    private Game currentGame;
    private GameScreen currentGameScreen;

    private Scene scene;

    public GameScreenStarter(Scene scene) {
        this.scene = scene;
        waitForPlayers();
    }

    public void waitForPlayers() {
        PlayerWaitingScreen currentGameScreen = PlayerWaitingScreen.getInstance();
        currentGameScreen.setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("game-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );
        screenController = NameScreenController.getInstance(currentGame, currentGameScreen);
        currentGameScreen.initialize();
    }


    private Game initializeGameScreen(List<PlayerInfo> playerInfoList, List<CardMessage> cardMessages) {
        return switch (GameContextCore.getGameType()) {
            case DAKETI -> initializeDakettiScreen(playerInfoList, cardMessages);
            case THULLA -> initializeThullaScreen(playerInfoList, cardMessages);
        };
    }

    private Game initializeDakettiScreen(List<PlayerInfo> playerInfoList, List<CardMessage> cardMessages) {
        Daketi daketi = new Daketi(playerInfoList, (DaketiPlayer) GameContextCore.currentPlayer);
        daketi.startGame(cardMessages, GameContextCore.currentPlayer.getPlayerNumber());
        currentGameScreen = new DaketiScreen();

        currentGameScreen.setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("game-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );
        screenController = DaketiScreenController.getInstance(daketi, currentGameScreen);
        return daketi;
    }

    private Game initializeThullaScreen(List<PlayerInfo> playerInfoList, List<CardMessage> cardMessages) {
        Thulla thulla = new Thulla();
        thulla.startGame(cardMessages, GameContextCore.currentPlayer.getPlayerNumber());
        currentGameScreen = new ThullaScreen();
        currentGameScreen.setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("game-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );
        screenController = ThullaScreenController.getInstance(thulla, currentGameScreen);
        return thulla;
    }


    private Game getBluffGame() {
        return null;
    }

    public void startGame(List<PlayerInfo> playerInfoList, List<CardMessage> cardMessages) {
        PlayerWaitingScreen.getInstance().startGameCountdown();
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    PlayerWaitingScreen.getInstance().cleanup();
                    currentGame = initializeGameScreen(playerInfoList, cardMessages);
                    scene.setRoot(currentGameScreen);
                    currentGameScreen.initialize(currentGame, playerInfoList);
                    screenController.startGame();
                });
            }
        }, 3000);
    }

    public void clear() {
        screenController.clear();
        currentGame = null;
    }
}
