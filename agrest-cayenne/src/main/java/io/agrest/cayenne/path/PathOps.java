package io.agrest.cayenne.path;

import org.apache.cayenne.exp.DbPathExp;
import org.apache.cayenne.exp.ObjPathExp;
import org.apache.cayenne.exp.PathExp;
import org.apache.cayenne.exp.path.CayennePath;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

/**
 * @since 5.0
 */
public class PathOps {

    /**
     * @since 5.0
     */
    public static PathExp parsePath(String path) {
        return path.startsWith(DbPathExp.DB_PREFIX)
                ? new DbPathExp(path.substring(DbPathExp.DB_PREFIX.length()))
                : new ObjPathExp(path);
    }

    // TODO: any chance of caching resolved concat paths that is faster than rebuilding them from scratch?

    /**
     * @since 5.0
     */
    public static DbPathExp resolveAsDbPath(ObjEntity entity, PathExp p) {
        return switch (p) {
            case DbPathExp dbPath -> dbPath;
            case ObjPathExp objPath -> resolveAsDbPath(entity, objPath);
            default -> throw new IllegalArgumentException("Unexpected path type: " + p.expName());
        };
    }

    /**
     * @since 5.0
     */
    public static PathExp concat(ObjEntity entity, PathExp p1, PathExp p2) {
        return switch (p1) {
            case DbPathExp dbPath -> concatWithDbPath(entity, dbPath, p2);
            case ObjPathExp objPath -> concatWithObjPath(entity, objPath, p2);
            default -> throw new IllegalArgumentException("Unexpected p1 type: " + p1.expName());
        };
    }

    /**
     * @since 5.0
     */
    public static PathExp concatWithDbPath(ObjEntity entity, DbPathExp p1, PathExp p2) {
        return switch (p2) {
            case DbPathExp dbPath -> new DbPathExp(p1.getPath().dot(dbPath.getPath()));
            case ObjPathExp objPath -> new DbPathExp(p1.getPath().dot(resolveAsDbPath(entity, objPath).getPath()));
            default -> throw new IllegalArgumentException("Unexpected p2 type: " + p2.expName());
        };
    }

    /**
     * @since 5.0
     */
    public static PathExp concatWithObjPath(ObjEntity entity, ObjPathExp p1, PathExp p2) {
        return switch (p2) {
            case DbPathExp dbPath -> concatWithDbPath(entity, resolveAsDbPath(entity, p1), dbPath);
            case ObjPathExp objPath -> new ObjPathExp(p1.getPath().dot(objPath.getPath()));
            default -> throw new IllegalArgumentException("Unexpected p2 type: " + p2.expName());
        };
    }

    private static DbPathExp resolveAsDbPath(ObjEntity entity, ObjPathExp objPath) {

        CayennePath dbPath = CayennePath.EMPTY_PATH;
        for (Object component : entity.resolvePath(objPath.getExpandedPath())) {
            switch (component) {
                case ObjRelationship r -> dbPath = dbPath.dot(r.getDbRelationshipPath());
                case ObjAttribute a -> {
                    CayennePath attributePath = a.getDbAttributePath();
                    if (attributePath != null) {
                        dbPath = dbPath.dot(attributePath);
                    }
                }
                default -> throw new IllegalArgumentException("Unexpected path component: " + component);
            }
        }

        return new DbPathExp(dbPath);
    }
}
