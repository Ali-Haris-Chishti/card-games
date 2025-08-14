package com.ahccode.cards.ui.daketi;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.ui.component.CardInStackBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.List;

public class DaketiGameFinishedScreen extends StackPane {

    public DaketiGameFinishedScreen(List<Card> teamAStack, List<Card> teamBStack, int teamAScore, int teamBScore) {
        // Calculate scores (assuming Card has a getValue() method)

        String resultText;
        if (teamAScore > teamBScore) {
            resultText = "Team Green Wins!";
        } else if (teamBScore > teamAScore) {
            resultText = "Team Blue Wins!";
        } else {
            resultText = "It's a Tie!";
        }

        Label resultLabel = new Label(resultText);
        resultLabel.setFont(Font.font("Arial", 30));
        resultLabel.setTextFill(Color.DARKBLUE);
        resultLabel.setPadding(new Insets(20, 0, 30, 0));

        CardInStackBox teamAStackBox = new CardInStackBox(teamAStack, Team.TEAM_GREEN, -1, 30);
        CardInStackBox teamBStackBox = new CardInStackBox(teamBStack, Team.TEAM_BLUE, -1, 30);

        Label teamALabel = new Label("Team Green Score: " + teamAScore);
        teamALabel.setFont(Font.font(18));
        teamALabel.setTextFill(Color.DARKGREEN);

        Label teamBLabel = new Label("Team Blue Score: " + teamBScore);
        teamBLabel.setFont(Font.font(18));
        teamBLabel.setTextFill(Color.DARKBLUE);

        VBox teamASection = new VBox(10, teamAStackBox, teamALabel);
        teamASection.setAlignment(Pos.CENTER);

        VBox teamBSection = new VBox(10, teamBStackBox, teamBLabel);
        teamBSection.setAlignment(Pos.CENTER);

        VBox mainBox = new VBox(30, resultLabel, teamASection, teamBSection);
        mainBox.setAlignment(Pos.CENTER);
        mainBox.setPadding(new Insets(40));

        // Optional: Add background
        setBackground(new Background(new BackgroundFill(Color.rgb(240, 248, 255), CornerRadii.EMPTY, Insets.EMPTY)));

        getChildren().add(mainBox);
    }
}
