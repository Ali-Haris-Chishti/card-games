package com.ahccode.client.game;

import com.ahccode.common.card.Card;
import com.ahccode.common.card.CardFamily;
import com.ahccode.common.card.CardNumber;
import com.ahccode.common.network.CardMessage;

import java.util.List;
import java.util.Optional;

public abstract class Game{
    public abstract void startGame(List<CardMessage> cardMessages, int playerNumber);

    public abstract Optional<Card> getCardByFamilyAndNumber(CardFamily family, CardNumber number);
}
