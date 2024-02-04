package io.agrest.cayenne.path;

import io.agrest.AgException;
import io.agrest.PathConstants;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class EntityPathCache {

    private final ObjEntity entity;
    private final Map<String, PathDescriptor> pathCache;

    EntityPathCache(ObjEntity entity) {
        this.entity = entity;
        this.pathCache = new ConcurrentHashMap<>();

        // Immediately cache a special entry matching the "id" constant. Can only do that if the ID is made of a
        // single "part", as only such an ID would resolve to a single Cayenne path

        // TODO: this is a hack - we are treating "id" as a "virtual" attribute, as there's generally no "id"
        //   property in AgEntity. See the same note in EncodablePropertyFactory

        // store "pks" in a var for reuse, as ObjEntity would rebuild them on every call
        Collection<ObjAttribute> pks = entity.getPrimaryKeys();
        if (pks.size() == 1) {

            // TODO: here we are ignoring the name of the ID attribute and are using the fixed name instead.
            //  Same issue as the above
            ObjAttribute a = pks.iterator().next();
            pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor(a.getType(), new ASTDbPath(a.getDbAttributePath()), true));
        }
    }

    PathDescriptor getOrCreate(String agPath) {
        return pathCache.computeIfAbsent(agPath, p -> computePathDescriptor(agPath));
    }

    private PathDescriptor computePathDescriptor(String agPath) {

        Object last = lastPathComponent(entity, agPath);

        if (last instanceof ObjAttribute) {
            ObjAttribute a = (ObjAttribute) last;
            return a.isPrimaryKey()
                    ? new PathDescriptor(a.getType(), new ASTDbPath(a.getDbAttributePath()), true)
                    : new PathDescriptor(a.getType(), new ASTObjPath(agPath), true);
        }

        if (last instanceof DbAttribute) {
            DbAttribute a = (DbAttribute) last;
            return new PathDescriptor(TypesMapping.getJavaBySqlType(a.getType()), new ASTDbPath(a.getName()), true);
        }

        ObjRelationship relationship = (ObjRelationship) last;
        return new PathDescriptor(relationship.getTargetEntity().getClassName(), new ASTObjPath(agPath), false);
    }

    Object lastPathComponent(ObjEntity entity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw AgException.badRequest("Invalid path '%s' for '%s' - can't start with a dot", path, entity.getName());
        }

        if (dot == path.length() - 1) {
            throw AgException.badRequest("Invalid path '%s' for '%s' - can't end with a dot", path, entity.getName());
        }

        if (dot > 0) {
            String segment = toRelationshipName(path.substring(0, dot));

            // followed by dot, so must be a relationship
            ObjRelationship relationship = entity.getRelationship(segment);
            if (relationship == null) {
                throw AgException.badRequest("Invalid path '%s' for '%s'. Not a relationship",
                        path,
                        entity.getName());
            }

            ObjEntity targetEntity = relationship.getTargetEntity();
            return lastPathComponent(targetEntity, path.substring(dot + 1));
        }

        ObjAttribute attribute = entity.getAttribute(path);
        if (attribute != null) {
            return attribute;
        }

        ObjRelationship relationship = entity.getRelationship(toRelationshipName(path));
        if (relationship != null) {
            return relationship;
        }

        if (path.startsWith(ASTDbPath.DB_PREFIX)) {
            DbAttribute dbAttribute = entity.getDbEntity().getAttribute(path.substring(ASTDbPath.DB_PREFIX.length()));
            if (dbAttribute != null) {
                return dbAttribute;
            }
        }

        throw AgException.badRequest("Invalid path '%s' for '%s'", path, entity.getName());
    }

    private String toRelationshipName(String pathSegment) {
        return pathSegment.endsWith("+") ? pathSegment.substring(0, pathSegment.length() - 1) : pathSegment;
    }
}
