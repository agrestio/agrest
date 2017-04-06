package com.nhl.link.rest.runtime.parser;

import java.util.Map;

public interface EntityJsonVisitor {

    void beginObject();

    void visitId(String name, Object value);

    void visitId(Map<String, Object> value);

    void visitAttribute(String name, Object value);

    void visitRelationship(String name, Object relatedId);

    void endObject();
}
