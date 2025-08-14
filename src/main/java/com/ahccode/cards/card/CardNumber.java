package com.ahccode.cards.card;

public enum CardNumber {
    ACE(20, 13),
    KING(10, 12),
    QUEEN(10, 11),
    JACK(10, 10),
    TWO(5, 1),
    THREE(5, 2),
    FOUR(5, 3),
    FIVE(5, 4),
    SIX(5, 5),
    SEVEN(5, 6),
    EIGHT(5, 7),
    NINE(5, 8),
    TEN(10, 9);

    CardNumber(int dakettiScore, int thulaOrder) {
        this.dakettiScore = dakettiScore;
        this.thulaOrder = thulaOrder;
    }

    private int dakettiScore;

    private int thulaOrder;

    public int getDakettiScore() {
        return dakettiScore;
    }

    public int getThulaOrder() {
        return thulaOrder;
    }
}
