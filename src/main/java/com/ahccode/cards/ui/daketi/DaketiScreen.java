package com.ahccode.cards.ui.daketi;

import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.ui.GameScreen;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.component.CardInStackBox;

import java.util.ArrayList;
import java.util.List;

import static com.ahccode.cards.card.game.Team.*;

public class DaketiScreen extends GameScreen {

    private Daketi daketi;

    List<CardInHandBox> handCardBoxes;
    List<CardInStackBox> stackCardBoxes;

    public CardInHandBox getHandCardBoxFromIndex(int index) {
        return handCardBoxes.get(index);
    }

    public CardInStackBox getStackCardBoxFromIndex(int index) {
        return stackCardBoxes.get(index);
    }

    public DaketiScreen() {
    }

    @Override
    public void initialize(Game game) {
        this.daketi = (Daketi) game;
        handCardBoxes = new ArrayList<>();
        stackCardBoxes = new ArrayList<>();

        Team [] teams = {TEAM_GREEN, TEAM_BLUE};
        for (int i = 0; i < 4; i ++) {
            handCardBoxes.add(new CardInHandBox(daketi.getCardsOfPlayer(i), teams[i%2], i));
            getChildren().add(handCardBoxes.get(i));
        }

        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackA(), TEAM_GREEN, -1));
        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackB(), TEAM_BLUE, -1));

        stackCardBoxes.add(new CardInStackBox(daketi.getRemainingCards(), NONE, -1));

        for (CardInStackBox stackBox : stackCardBoxes)
            getChildren().add(stackBox);

        handCardBoxes.add(new CardInHandBox(daketi.getCardsInCenter(), NONE, -1));

        getChildren().add(handCardBoxes.getLast());

        // Position on layout pass
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

        // Stack boxes
        CardInStackBox stackABox = stackCardBoxes.get(0); // top center
        CardInStackBox stackBBox = stackCardBoxes.get(1); // bottom center
        CardInStackBox remainingBox = stackCardBoxes.get(2); // central stack

        double stackAWidth = stackABox.prefWidth(-1);
        double stackAHeight = stackABox.prefHeight(-1);

        double stackBWidth = stackBBox.prefWidth(-1);
        double stackBHeight = stackBBox.prefHeight(-1);

        double remainingWidth = remainingBox.prefWidth(-1);
        double remainingHeight = remainingBox.prefHeight(-1);

        // Position hand boxes
        topLeftBox.resizeRelocate(padding, padding, topLeftWidth, topLeftHeight);
        topRightBox.resizeRelocate(width - topRightWidth - padding, padding, topRightWidth, topRightHeight);
        bottomRightBox.resizeRelocate(width - bottomRightWidth - padding, height - bottomRightHeight - padding, bottomRightWidth, bottomRightHeight);
        bottomLeftBox.resizeRelocate(padding, height - bottomLeftHeight - padding, bottomLeftWidth, bottomLeftHeight);

        // Stack A: Top center
        stackABox.resizeRelocate((width - stackAWidth) / 2, padding, stackAWidth, stackAHeight);

        // Stack B: Bottom center
        stackBBox.resizeRelocate((width - stackBWidth) / 2, height - stackBHeight - padding, stackBWidth, stackBHeight);

        // Remaining cards and center cards: middle with spacing
        double totalCenterWidth = remainingWidth + spacing + centerHandWidth;
        double centerX = (width - totalCenterWidth) / 2;
        double centerY = (height - Math.max(remainingHeight, centerHandHeight)) / 2;

        remainingBox.resizeRelocate(centerX, centerY, remainingWidth, remainingHeight);
        centerHandBox.resizeRelocate(centerX + remainingWidth + spacing, centerY, centerHandWidth, centerHandHeight);
    }

}
