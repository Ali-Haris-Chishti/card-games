package com.ahccode.common.network;

import com.ahccode.common.card.*;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;

@ToString
public class CardMessage implements Serializable {
    @Serial
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
