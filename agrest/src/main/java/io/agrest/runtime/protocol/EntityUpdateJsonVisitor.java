package io.agrest.runtime.protocol;

import java.util.Map;

public interface EntityUpdateJsonVisitor {

    void beginObject();

    void visitId(String name, Object value);

    void visitId(Map<String, Object> value);

    void visitAttribute(String name, Object value);

    void visitRelationship(String name, Object relatedId);

    void endObject();
}
