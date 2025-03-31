package io.agrest.cayenne.cayenne.inheritance;

import io.agrest.annotation.AgAttribute;
import io.agrest.cayenne.cayenne.inheritance.auto._Aie1Super;

public abstract class Aie1Super extends _Aie1Super {

    @AgAttribute(readable = false)
    @Override
    public String getA0() {
        return super.getA0();
    }
}
