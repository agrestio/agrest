package io.agrest.runtime.protocol;

public interface EntityUpdateJsonVisitor {

    void beginObject();

    void visitId(String name, Object value);

    void visitAttribute(String name, Object value);

    void visitRelationship(String name, Object relatedId);

    void endObject();
}
