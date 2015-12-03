package com.nhl.link.rest.runtime.parser.pointer;

public interface PointerContext {

    Object resolvePointer(LrPointer pointer, Object baseObject);
}
