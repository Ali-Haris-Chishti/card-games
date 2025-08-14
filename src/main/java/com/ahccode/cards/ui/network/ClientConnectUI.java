package com.ahccode.cards.ui.network;

import com.ahccode.cards.card.game.Player;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.context.GameContextUI;
import com.ahccode.cards.card.game.daketi.DaketiPlayer;
import com.ahccode.cards.network.GameClient;
import com.ahccode.cards.ui.StartScreen;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class ClientConnectUI extends StackPane {

    private static ClientConnectUI instance;

    private final Stage stage;
    private final Scene scene;

    private ClientConnectUI(Stage stage, Scene scene) throws IOException {
        this.stage = stage;
        this.scene = scene;

//        createClient();

        // ===== Background =====
        setBackground(
                new Background(
                        new BackgroundImage(
                                new Image(Objects.requireNonNull(getClass().getResourceAsStream("connect-bg.jpg"))),
                                BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
                                BackgroundPosition.CENTER,
                                new BackgroundSize(GameContextUI.SCREEN_WIDTH, GameContextUI.SCREEN_HEIGHT, true, true, true, true)
                        )
                )
        );

        // ===== Main Content Container =====
        VBox formContainer = new VBox(15);
        formContainer.setAlignment(Pos.CENTER);
        formContainer.setPadding(new Insets(30));
        formContainer.setMaxWidth(400);
        formContainer.setBackground(new Background(new BackgroundFill(
                javafx.scene.paint.Color.rgb(0, 0, 0, 0.6), new CornerRadii(15), Insets.EMPTY
        )));

        // ===== Title =====
        Label title = new Label("Connect to Daketi Server");
        title.setStyle("-fx-font-size: 28px; -fx-text-fill: white; -fx-font-weight: bold;");

        // ===== Player Name Field =====
        TextField nameField = new TextField();
        nameField.setPromptText("Your Name");
        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Host Field =====
        TextField hostField = new TextField("localhost");
        hostField.setPromptText("Server Host");
        hostField.setMaxWidth(Double.MAX_VALUE);
        hostField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Port Field =====
        TextField portField = new TextField("5000");
        portField.setPromptText("Server Port");
        portField.setMaxWidth(Double.MAX_VALUE);
        portField.setStyle("-fx-background-radius: 8; -fx-padding: 10; -fx-font-size: 14px;");

        // ===== Connect Button =====
        Button connectButton = new Button("Connect");
        connectButton.setMaxWidth(Double.MAX_VALUE);
        connectButton.setStyle(
                "-fx-background-color: linear-gradient(#3b9d3b, #256d25);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10;"
        );
        connectButton.setOnMouseEntered(e -> connectButton.setStyle(
                "-fx-background-color: linear-gradient(#4ed14e, #2f902f);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10;"
        ));
        connectButton.setOnMouseExited(e -> connectButton.setStyle(
                "-fx-background-color: linear-gradient(#3b9d3b, #256d25);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10;"
        ));

        connectButton.setOnAction(e -> {
            String host = hostField.getText();
            int port;
            try {
                port = Integer.parseInt(portField.getText());
            } catch (NumberFormatException ex) {
                showAlert("Invalid Port", "Please enter a valid port number.");
                return;
            }

            GameClient client = null;
            try {
                client = new GameClient(host, port);
                log.info("Creating Player for Client");
                Player player = new DaketiPlayer(0, nameField.getText(), client);
                GameContextCore.currentPlayer = player;
                scene.setRoot(StartScreen.getInstance(stage, scene));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

//            GameClient client = new GameClient(host, port);
//            Player player = new Player(1, nameField.getText(), client);
//            scene.setRoot(StartScreen.getInstance(stage, scene, player));
        });

        // ===== Assemble Form =====
        formContainer.getChildren().addAll(title, nameField, hostField, portField, connectButton);
        getChildren().add(formContainer);
    }

    public static ClientConnectUI getInstance(Stage stage, Scene scene) throws IOException {
        if (instance == null) {
            instance = new ClientConnectUI(stage, scene);
        }
        return instance;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void createClient() throws IOException {
        GameClient client = new GameClient("localhost", 5000);
        System.out.println("Creating Player for Client");
        Player player = new DaketiPlayer(0, "haris", client);
        GameContextCore.currentPlayer = player;
        scene.setRoot(StartScreen.getInstance(stage, scene));
    }

}
