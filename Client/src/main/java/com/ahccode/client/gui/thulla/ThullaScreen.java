package com.ahccode.client.gui.thulla;

import com.ahccode.client.game.Game;
import com.ahccode.client.game.Team;
import com.ahccode.client.game.thulla.Thulla;
import com.ahccode.client.gui.GameScreen;
import com.ahccode.client.gui.component.CardInHandBox;
import com.ahccode.common.network.PlayerInfo;
import javafx.geometry.Insets;

import java.util.ArrayList;
import java.util.List;

public class ThullaScreen extends GameScreen {

    private Thulla thulla;
    private final List<CardInHandBox> handCardBoxes = new ArrayList<>();
    private final int NUMBER_OF_PLAYERS = 4;

    private static final double PADDING = 30;

    public CardInHandBox getCardInHandBoxFromIndex(int index) {
        return handCardBoxes.get(index);
    }

    public ThullaScreen() {
    }

    @Override
    public void initialize(Game game, List<PlayerInfo> playerInfoList) {
        this.thulla = (Thulla) game;
        setPadding(new Insets(PADDING));

        for (int i = 0; i < NUMBER_OF_PLAYERS; i++) {
            handCardBoxes.add(new CardInHandBox(thulla.getCardsOfPlayerNumber(i), Team.NONE, i, playerInfoList.get(i).getName()));
            getChildren().add(handCardBoxes.get(i));
        }

        handCardBoxes.add(new CardInHandBox(new ArrayList<>(), Team.NONE, -1, null));
        getChildren().add(handCardBoxes.get(4));

        layoutBoundsProperty().addListener((obs, oldVal, newVal) -> layoutChildren());
    }

    @Override
    protected void layoutChildren() {
        double padding = 20;
        double spacing = 10;

        double width = getWidth();
        double height = getHeight();

        // Hand boxes (4 players + 1 center)
        CardInHandBox topLeftBox = handCardBoxes.get(0);
        CardInHandBox topRightBox = handCardBoxes.get(1);
        CardInHandBox bottomRightBox = handCardBoxes.get(2);
        CardInHandBox bottomLeftBox = handCardBoxes.get(3);
        CardInHandBox centerHandBox = handCardBoxes.get(4); // cards in center

        double topLeftWidth = topLeftBox.prefWidth(-1);
        double topLeftHeight = topLeftBox.prefHeight(-1);

        double topRightWidth = topRightBox.prefWidth(-1);
        double topRightHeight = topRightBox.prefHeight(-1);

        double bottomRightWidth = bottomRightBox.prefWidth(-1);
        double bottomRightHeight = bottomRightBox.prefHeight(-1);

        double bottomLeftWidth = bottomLeftBox.prefWidth(-1);
        double bottomLeftHeight = bottomLeftBox.prefHeight(-1);

        double centerHandWidth = centerHandBox.prefWidth(-1);
        double centerHandHeight = centerHandBox.prefHeight(-1);

        // Position hand boxes
        topLeftBox.resizeRelocate(padding, padding, topLeftWidth, topLeftHeight);
        topRightBox.resizeRelocate(width - topRightWidth - padding, padding, topRightWidth, topRightHeight);
        bottomRightBox.resizeRelocate(width - bottomRightWidth - padding, height - bottomRightHeight - padding, bottomRightWidth, bottomRightHeight);
        bottomLeftBox.resizeRelocate(padding, height - bottomLeftHeight - padding, bottomLeftWidth, bottomLeftHeight);

        // Remaining cards and center cards: middle with spacing
        double totalCenterWidth = spacing + centerHandWidth;
        double centerX = (width - totalCenterWidth) / 2;
        double centerY = (height - centerHandHeight) / 2;

        centerHandBox.resizeRelocate(centerX + spacing, centerY, centerHandWidth, centerHandHeight);
    }
}
