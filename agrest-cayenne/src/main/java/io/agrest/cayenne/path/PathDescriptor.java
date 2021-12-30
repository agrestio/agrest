package io.agrest.cayenne.path;

import org.apache.cayenne.exp.parser.ASTPath;

public class PathDescriptor {

    private final boolean attributeOrId;
    private final ASTPath path;
    private final Class<?> type;

    public PathDescriptor(Class<?> type, ASTPath path, boolean attributeOrId) {
        this.path = path;
        this.type = type;
        this.attributeOrId = attributeOrId;
    }

    /**
     * @since 5.0
     */
    public boolean isAttributeOrId() {
        return attributeOrId;
    }

    public Class<?> getType() {
        return type;
    }

    public ASTPath getPathExp() {
        return path;
    }
}
