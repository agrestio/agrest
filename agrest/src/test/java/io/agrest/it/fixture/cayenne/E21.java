package io.agrest.it.fixture.cayenne;

import io.agrest.annotation.AgId;
import io.agrest.it.fixture.cayenne.auto._E21;

public class E21 extends _E21 {

    private static final long serialVersionUID = 1L;

    @AgId
    @Override
    public Integer getAge() {
        return super.getAge();
    }

    @AgId
    @Override
    public String getName() {
        return super.getName();
    }

}
