package com.nhl.link.rest.runtime.parser.pointer;

public interface PointerContext {

    Object resolveObject(Class<?> type, Object id);

    /**
     * @param baseObject Base object for resolving the specified attribute.
     *                   Should be from this PointerContext's Cayenne context
     */
    Object resolveProperty(Class<?> type, String propertyName, Object baseObject);
}
