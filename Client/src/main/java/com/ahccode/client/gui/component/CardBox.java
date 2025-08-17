package com.ahccode.client.gui.component;

import com.ahccode.client.game.Team;
import com.ahccode.client.gui.controller.DaketiScreenController;
import com.ahccode.client.gui.controller.ScreenController;
import com.ahccode.client.gui.controller.ThullaScreenController;
import com.ahccode.common.card.Card;
import com.ahccode.common.context.GameContextCore;
import javafx.scene.layout.Pane;

import java.util.List;

import static com.ahccode.common.game.GameType.*;

public abstract class CardBox extends Pane {

    protected static final double CARD_WIDTH = 150;
    protected static final double CARD_HEIGHT = 210;
    protected final double CARD_OVERLAP;

    protected ScreenController screenController;

    protected final List<Card> cards;

    protected final Team team;
    protected final int playerNumber;



    protected CardBox(List<Card> cards, Team team, int playerNumber, int cardOverlap) {
        this.cards = cards;
        this.team = team;
        this.playerNumber = playerNumber;
        this.CARD_OVERLAP = cardOverlap;
        this.screenController = assignControllerBasedOnGameType();
    }

    private ScreenController assignControllerBasedOnGameType() {
        return switch (GameContextCore.getGameType()) {
            case DAKETI -> DaketiScreenController.getInstance(null, null);
            case THULLA -> ThullaScreenController.getInstance(null, null);
        };
    }

    public abstract void addCard(Card card);

}
