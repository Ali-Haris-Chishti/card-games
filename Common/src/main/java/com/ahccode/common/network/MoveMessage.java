package com.ahccode.common.network;

import com.ahccode.common.card.CardFamily;
import com.ahccode.common.card.CardNumber;
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
