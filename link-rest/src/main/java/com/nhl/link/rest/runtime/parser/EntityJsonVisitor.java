package com.nhl.link.rest.runtime.parser;

public interface EntityJsonVisitor {

    void beginObject();

    void visitId(String name, Object value);

    void visitAttribute(String name, Object value);

    void visitRelationship(String name, Object relatedId);

    void endObject();
}
