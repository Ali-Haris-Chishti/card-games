package com.ahccode.cards.ui.daketi;

import com.ahccode.cards.card.game.Game;
import com.ahccode.cards.card.game.PlayerInfo;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.card.game.daketi.Daketi;
import com.ahccode.cards.ui.GameScreen;
import com.ahccode.cards.ui.component.CardInHandBox;
import com.ahccode.cards.ui.component.CardInStackBox;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static com.ahccode.cards.card.game.Team.*;

@Slf4j
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
    public void initialize(Game game, List<PlayerInfo> playerInfoList) {
        log.info("Players Info: {}", playerInfoList);
        this.daketi = (Daketi) game;
        handCardBoxes = new ArrayList<>();
        stackCardBoxes = new ArrayList<>();

        Team[] teams = {TEAM_GREEN, TEAM_BLUE};

        // Create hand card boxes with player names
        for (int i = 0; i < 4; i++) {
            String playerName = playerInfoList.get(i).getName();
            handCardBoxes.add(new CardInHandBox(daketi.getCardsOfPlayer(i), teams[i % 2], i, playerName));
            getChildren().add(handCardBoxes.get(i));
        }

        // Create stack boxes
        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackA(), TEAM_GREEN, -1));
        stackCardBoxes.add(new CardInStackBox(daketi.getCardStackB(), TEAM_BLUE, -1));
        stackCardBoxes.add(new CardInStackBox(daketi.getRemainingCards(), NONE, -1));

        for (CardInStackBox stackBox : stackCardBoxes)
            getChildren().add(stackBox);

        // Center cards box (no player name needed - pass null)
        handCardBoxes.add(new CardInHandBox(daketi.getCardsInCenter(), NONE, -1, null));
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

        // Hand boxes
        CardInHandBox topLeftBox = handCardBoxes.get(0);
        CardInHandBox topRightBox = handCardBoxes.get(1);
        CardInHandBox bottomRightBox = handCardBoxes.get(2);
        CardInHandBox bottomLeftBox = handCardBoxes.get(3);
        CardInHandBox centerHandBox = handCardBoxes.get(4);

        double tlw = topLeftBox.prefWidth(-1), tlh = topLeftBox.prefHeight(-1);
        double trw = topRightBox.prefWidth(-1), trh = topRightBox.prefHeight(-1);
        double brw = bottomRightBox.prefWidth(-1), brh = bottomRightBox.prefHeight(-1);
        double blw = bottomLeftBox.prefWidth(-1), blh = bottomLeftBox.prefHeight(-1);
        double chw = centerHandBox.prefWidth(-1), chh = centerHandBox.prefHeight(-1);

        // Stack boxes
        CardInStackBox stackABox = stackCardBoxes.get(0);
        CardInStackBox stackBBox = stackCardBoxes.get(1);
        CardInStackBox remainingBox = stackCardBoxes.get(2);

        double saw = stackABox.prefWidth(-1), sah = stackABox.prefHeight(-1);
        double sbw = stackBBox.prefWidth(-1), sbh = stackBBox.prefHeight(-1);
        double rw = remainingBox.prefWidth(-1), rh = remainingBox.prefHeight(-1);

        // Position hand boxes (they now include the name labels internally)
        topLeftBox.resizeRelocate(padding, padding, tlw, tlh);
        topRightBox.resizeRelocate(width - trw - padding, padding, trw, trh);
        bottomRightBox.resizeRelocate(width - brw - padding, height - brh - padding, brw, brh);
        bottomLeftBox.resizeRelocate(padding, height - blh - padding, blw, blh);

        // Stack A: Top center (adjust for increased hand box height)
        stackABox.resizeRelocate((width - saw) / 2, padding, saw, sah);

        // Stack B: Bottom center (adjust for increased hand box height)
        stackBBox.resizeRelocate((width - sbw) / 2, height - sbh - padding, sbw, sbh);

        // Remaining cards and center cards
        double totalCenterWidth = rw + spacing + chw;
        double centerX = (width - totalCenterWidth) / 2;
        double centerY = (height - Math.max(rh, chh)) / 2;

        remainingBox.resizeRelocate(centerX, centerY, rw, rh);
        centerHandBox.resizeRelocate(centerX + rw + spacing, centerY, chw, chh);
    }

}
