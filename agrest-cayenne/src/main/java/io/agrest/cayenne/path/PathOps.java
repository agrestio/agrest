package io.agrest.cayenne.path;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.util.CayenneMapEntry;

import java.util.Iterator;

/**
 * @since 5.0
 */
public class PathOps {

    /**
     * @since 5.0
     */
    public static ASTPath parsePath(String path) {
        return path.startsWith(ASTDbPath.DB_PREFIX)
                ? new ASTDbPath(path.substring(ASTDbPath.DB_PREFIX.length()))
                : new ASTObjPath(path);
    }


    // TODO: any chance of caching resolved concat paths that is faster than rebuilding them from scratch?

    /**
     * @since 5.0
     */
    public static ASTDbPath resolveAsDbPath(ASTPath p) {

        return switch (p.getType()) {
            case Expression.DB_PATH -> (ASTDbPath) p;
            case Expression.OBJ_PATH -> resolveAsDbPath(p);
            default -> throw new IllegalArgumentException("Unexpected p type: " + p.getType());
        };
    }

    /**
     * @since 5.0
     */
    public static ASTPath concat(ObjEntity entity, ASTPath p1, ASTPath p2) {

        return switch (p1.getType()) {
            case Expression.DB_PATH -> concatWithDbPath(entity, (ASTDbPath) p1, p2);
            case Expression.OBJ_PATH -> concatWithObjPath(entity, (ASTObjPath) p1, p2);
            default -> throw new IllegalArgumentException("Unexpected p1 type: " + p1.getType());
        };
    }

    /**
     * @since 5.0
     */
    public static ASTPath concatWithDbPath(ObjEntity entity, ASTDbPath p1, ASTPath p2) {

        return switch (p2.getType()) {
            case Expression.DB_PATH -> new ASTDbPath(p1.getPath() + "." + p2.getPath());
            case Expression.OBJ_PATH -> {
                ASTDbPath p2DB = resolveAsDbPath(entity, (ASTObjPath) p2);
                yield new ASTDbPath(p1.getPath() + "." + p2DB.getPath());
            }
            default -> throw new IllegalArgumentException("Unexpected p2 type: " + p2.getType());
        };
    }

    /**
     * @since 5.0
     */
    public static ASTPath concatWithObjPath(ObjEntity entity, ASTObjPath p1, ASTPath p2) {

        return switch (p2.getType()) {
            case Expression.DB_PATH -> concatWithDbPath(entity, resolveAsDbPath(entity, p1), p2);
            case Expression.OBJ_PATH -> new ASTObjPath(p1.getPath() + "." + p2.getPath());
            default -> throw new IllegalArgumentException("Unexpected p2 type: " + p2.getType());
        };
    }

    private static ASTDbPath resolveAsDbPath(ObjEntity entity, ASTObjPath objPath) {

        StringBuilder buffer = new StringBuilder();
        Iterator<CayenneMapEntry> it = entity.resolvePathComponents(objPath);
        while (it.hasNext()) {

            CayenneMapEntry e = it.next();

            if (!buffer.isEmpty()) {
                buffer.append('.');
            }

            if (it.hasNext() || e instanceof ObjRelationship) {
                ObjRelationship r = (ObjRelationship) e;
                buffer.append(r.getDbRelationshipPath());
            } else {
                ObjAttribute a = (ObjAttribute) e;
                buffer.append(a.getDbAttributePath());
            }
        }

        return new ASTDbPath(buffer.toString());
    }
}
