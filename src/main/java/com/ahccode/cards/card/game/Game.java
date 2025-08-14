package com.ahccode.cards.card.game;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import com.ahccode.cards.network.message.CardMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

public abstract class Game{
    public abstract void startGame(List<CardMessage> cardMessages, int playerNumber);

    public abstract Optional<Card> getCardByFamilyAndNumber(CardFamily family, CardNumber number);
}
