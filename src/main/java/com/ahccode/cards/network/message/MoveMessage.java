package com.ahccode.cards.network.message;

import com.ahccode.cards.card.CardFamily;
import com.ahccode.cards.card.CardNumber;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@ToString
public class MoveMessage implements Serializable {
    private static final long serialVersionUID = 1L;

    public CardFamily family;
    public CardNumber number;
    public int fromIndex;
    public int toIndex;
    public int moveNumber;
    public boolean updateTurn;
}
