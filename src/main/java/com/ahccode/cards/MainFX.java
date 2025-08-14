package com.ahccode.cards;

import com.ahccode.cards.ui.network.ClientConnectUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class MainFX extends Application {

    public static void main(String[] args) {
        launch(args);
    }


    private Scene scene;

    private static Stage primaryStage;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        scene = new Scene(new Pane());
        scene.setRoot(ClientConnectUI.getInstance(stage, scene));
//        scene.setRoot(StartScreen.getInstance(stage, scene));
        stage.setTitle("Cards");
//        stage.setResizable(false);
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        stage.show();

        scene.setOnKeyPressed(e -> {
            if (e.isControlDown() && e.getCode() == KeyCode.Q) {
                System.exit(0);
            }
        });


    }
}
