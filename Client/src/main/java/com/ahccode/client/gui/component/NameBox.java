package com.ahccode.client.gui.component;

import com.ahccode.client.game.Team;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.ahccode.client.gui.component.CardBox.*;

public class NameBox extends StackPane {

    private final Label nameLabel;
    private Team team;
    private int playerNumber;

    public NameBox() {
        this("Waiting...");
    }

    public NameBox(String name) {
        this(Team.NONE, name);
    }

    public NameBox(Team team, String name) {
        // Size based on card dimensions
        double totalWidth = CARD_WIDTH * 2;
        double containerPadding = 20;
        setPrefSize(totalWidth + containerPadding * 2, CARD_HEIGHT / 2);
        setPadding(new Insets(containerPadding / 2));

        // Team color (default: red with opacity)
        // Background color with rounded corners
        Color teamColor = switch (team) {
            case TEAM_GREEN -> Color.rgb(0, 150, 0, 0.5);
            case TEAM_BLUE -> Color.rgb(0, 100, 255, 0.5);
            default -> Color.rgb(255, 30, 30, 0.65); // center: red-ish
        };
        setBackground(new Background(
                new BackgroundFill(teamColor, new CornerRadii(15), Insets.EMPTY)
        ));
        setBorder(new Border(new BorderStroke(
                teamColor.darker(), BorderStrokeStyle.SOLID,
                new CornerRadii(15), new BorderWidths(2)
        )));

        // Name label in center
        nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setStyle("-fx-effect: dropshadow( gaussian , rgba(0,0,0,0.7) , 3, 0 , 1 , 1 );");

        // Add label to the stack so it’s perfectly centered
        getChildren().add(nameLabel);
    }

    public void setName(String name) {
        nameLabel.setText(name);
    }

    public String getName() {
        return nameLabel.getText();
    }
}
