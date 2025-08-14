package com.ahccode.cards.network;

import java.util.ArrayList;

public class ObservableList<E> extends ArrayList<E> {
    private final Runnable onAdd;

    public ObservableList(Runnable onAdd) {
        this.onAdd = onAdd;
    }

    @Override
    public boolean add(E e) {
        boolean result = super.add(e);
        if (result) onAdd.run();
        return result;
    }

    @Override
    public void add(int index, E element) {
        super.add(index, element);
        onAdd.run();
    }
}
