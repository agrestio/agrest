package io.agrest.cayenne.path;

import org.apache.cayenne.exp.parser.ASTPath;

/**
 * A mapping of an Agrest property path to a Cayenne path.
 */
public class PathDescriptor {

    private final boolean attributeOrId;
    private final ASTPath path;
    private final String type;

    public PathDescriptor(String type, ASTPath path, boolean attributeOrId) {
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

    public String getType() {
        return type;
    }

    public ASTPath getPathExp() {
        return path;
    }
}
