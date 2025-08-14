package com.ahccode.cards.network.message;

import com.ahccode.cards.card.Card;
import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class CardMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public CardFamily family;
    public CardNumber number;

    public int sender;

    public CardMessage(CardFamily family, CardNumber number) {
        this(family, number, -1);
    }

    public CardMessage(CardFamily family, CardNumber number, int sender) {
        this.family = family;
        this.number = number;
        this.sender = sender;
    }

    public Card toCard() {
        return new Card(this.family, this.number, false, false);
    }

}
