package io.agrest.it.fixture.pojox;

import io.agrest.annotation.AgId;

public class PX1 {

    private int id;

    public PX1(int id) {
        this.id = id;
    }

    @AgId
    public int getId() {
        return id;
    }
}
