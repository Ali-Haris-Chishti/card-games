package com.ahccode.cards.ui.component;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.game.context.GameContextCore;
import com.ahccode.cards.card.game.Team;
import com.ahccode.cards.ui.controller.DaketiScreenController;
import com.ahccode.cards.ui.controller.ScreenController;
import com.ahccode.cards.ui.controller.ThullaScreenController;
import javafx.scene.layout.Pane;

import java.util.List;

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
