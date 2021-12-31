package io.agrest.cayenne.path;

import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;

/**
 * A mapping of an Agrest property path to a Cayenne path.
 */
public class PathDescriptor {

    private final boolean attributeOrId;
    private final ASTPath path;
    private final Class<?> type;

    public static ASTPath parsePath(String path) {
        return path.startsWith(ASTDbPath.DB_PREFIX)
                ? new ASTDbPath(path.substring(ASTDbPath.DB_PREFIX.length()))
                : new ASTObjPath(path);
    }

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
