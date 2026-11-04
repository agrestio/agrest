package io.agrest.cayenne.path;

import org.apache.cayenne.exp.PathExp;

/**
 * A mapping of an Agrest property path to a Cayenne path.
 */
public class PathDescriptor {

    private final boolean attributeOrId;
    private final PathExp path;
    private final String type;

    public PathDescriptor(String type, PathExp path, boolean attributeOrId) {
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

    public PathExp getPathExp() {
        return path;
    }
}
