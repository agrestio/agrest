package com.nhl.link.rest.runtime.parser.pointer;

interface LrPointerBuilder {

    /**
     * @param relationshipName To-one or to-many relationship name
     * @param id Related object's id
     */
    // There's hardly any point in specifying ID for to-one relationships,
    // but we don't restrict that and add a check that related object
    // has specified ID.
    LrPointerBuilder append(String relationshipName, Object id);

    /**
     * @param pathElement To-one relationship name, attribute name or a single ID
     */
    LrPointerBuilder append(String pathElement);

    LrPointer build();
}
