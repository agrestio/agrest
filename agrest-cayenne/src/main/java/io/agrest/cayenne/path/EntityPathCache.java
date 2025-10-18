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
import java.util.Collections;
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
        return getOrCreate(agPath, Collections.emptyMap());
    }

    PathDescriptor getOrCreate(String agPath, Map<String, String> aliases) {
        return pathCache.computeIfAbsent(agPath, p -> create(agPath, agPath, entity, aliases));
    }

    private PathDescriptor create(
            String path,
            String remainingPath,
            ObjEntity remainingPathRootEntity,
            Map<String, String> aliases,
            ObjRelationship... processedPath) {

        int dot = remainingPath.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw AgException.badRequest("Invalid path '%s' for '%s' - can't start with a dot", remainingPath, remainingPathRootEntity.getName());
        }

        if (dot == remainingPath.length() - 1) {
            throw AgException.badRequest("Invalid path '%s' for '%s' - can't end with a dot", remainingPath, remainingPathRootEntity.getName());
        }

        if (dot > 0) {
            String segment = toRelationshipName(remainingPath.substring(0, dot));
            if(aliases.containsKey(segment)) {
                segment = aliases.get(segment);
            }

            // followed by dot, so must be a relationship
            ObjRelationship relationship = remainingPathRootEntity.getRelationship(segment);
            if (relationship == null) {
                throw AgException.badRequest("Invalid path '%s' for '%s'. Not a relationship",
                        remainingPath,
                        remainingPathRootEntity.getName());
            }

            return create(
                    path,
                    remainingPath.substring(dot + 1),
                    relationship.getTargetEntity(),
                    aliases,
                    push(processedPath, relationship));
        }

        ObjAttribute attribute = remainingPathRootEntity.getAttribute(remainingPath);
        if (attribute != null) {
            if (attribute.isPrimaryKey()) {
                ASTDbPath dbPath = new ASTDbPath(toDbPath(processedPath, attribute.getDbAttributePath()));
                dbPath.setPathAliases(aliases);
                return new PathDescriptor(attribute.getType(), dbPath, true);
            } else {
                ASTObjPath objPath = new ASTObjPath(path);
                objPath.setPathAliases(aliases);
                return new PathDescriptor(attribute.getType(), objPath, true);
            }
        }

        if(aliases.containsKey(remainingPath)) {
            remainingPath = aliases.get(remainingPath);
        }
        ObjRelationship relationship = remainingPathRootEntity.getRelationship(toRelationshipName(remainingPath));
        if (relationship != null) {
            ASTObjPath objPath = new ASTObjPath(path);
            objPath.setPathAliases(aliases);
            return new PathDescriptor(relationship.getTargetEntity().getClassName(), objPath, false);
        }

        if (remainingPath.startsWith(ASTDbPath.DB_PREFIX)) {
            DbAttribute dbAttribute = remainingPathRootEntity.getDbEntity().getAttribute(remainingPath.substring(ASTDbPath.DB_PREFIX.length()));
            if (dbAttribute != null) {
                ASTDbPath dbPath = new ASTDbPath(toDbPath(processedPath, dbAttribute.getName()));
                dbPath.setPathAliases(aliases);
                return new PathDescriptor(
                        TypesMapping.getJavaBySqlType(dbAttribute.getType()),
                        dbPath,
                        true);
            }
        }

        // if a path is a relationship with an ".id" suffix, simply use the relationship
        if (PathConstants.ID_PK_ATTRIBUTE.equals(remainingPath) && processedPath != null && processedPath.length > 0) {
            ObjRelationship lastProcessed = processedPath[processedPath.length - 1];
            String strippedPath = path.substring(0, path.length() - PathConstants.ID_PK_ATTRIBUTE.length() - 1);
            ASTObjPath strippedObjPath = new ASTObjPath(strippedPath);
            strippedObjPath.setPathAliases(aliases);
            return new PathDescriptor(lastProcessed.getTargetEntity().getClassName(), strippedObjPath, false);
        }

        throw AgException.badRequest("Invalid path '%s' for '%s'", remainingPath, remainingPathRootEntity.getName());
    }

    private String toRelationshipName(String pathSegment) {
        return pathSegment.endsWith("+") ? pathSegment.substring(0, pathSegment.length() - 1) : pathSegment;
    }

    private static ObjRelationship[] push(ObjRelationship[] a, ObjRelationship r) {

        if (a == null || a.length == 0) {
            return new ObjRelationship[]{r};
        }

        ObjRelationship[] a1 = new ObjRelationship[a.length + 1];
        System.arraycopy(a, 0, a1, 0, a.length);
        a1[a.length] = r;
        return a1;
    }

    private static String toDbPath(ObjRelationship[] a, String lastDbAttribute) {
        if (a == null || a.length == 0) {
            return lastDbAttribute;
        }

        StringBuilder path = new StringBuilder();
        for (int i = 0; i < a.length; i++) {
            path.append(a[i].getDbRelationshipPath()).append(PathConstants.DOT);
        }

        return path.append(lastDbAttribute).toString();
    }
}
