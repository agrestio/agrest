package io.agrest.cayenne.path;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.exp.parser.ASTObjPath;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class EntityPathCache {

    private final AgEntity<?> entity;
    private final Map<String, PathDescriptor> pathCache;

    EntityPathCache(AgEntity<?> entity) {
        this.entity = entity;
        this.pathCache = new ConcurrentHashMap<>();

        // Immediately cache a special entry matching the "id" constant. Can only do that if the ID is made of a
        // single "part", as only such an ID would resolve to a single Cayenne path

        // TODO: this is a hack - we are treating "id" as a "virtual" attribute, as there's generally no "id"
        //   property in AgEntity. See the same note in EncodablePropertyFactory

        if (entity.getIdParts().size() == 1) {

            // TODO: here we are ignoring the name of the ID attribute and are using the fixed name instead.
            //  Same issue as the above
            AgIdPart id = entity.getIdParts().iterator().next();
            pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor(id.getType(), PathDescriptor.parsePath(id.getName()), true));
        }
    }

    PathDescriptor getOrCreate(String agPath) {
        return pathCache.computeIfAbsent(agPath, p -> computePathDescriptor(agPath));
    }

    private PathDescriptor computePathDescriptor(String agPath) {

        final Object last = lastPathComponent(entity, agPath);

        if (last instanceof AgAttribute) {
            return new PathDescriptor(((AgAttribute) last).getType(), new ASTObjPath(agPath), true);
        }

        if (last instanceof AgIdPart) {
            AgIdPart id = (AgIdPart) last;
            return new PathDescriptor(id.getType(), PathDescriptor.parsePath(id.getName()), true);
        }

        AgRelationship relationship = (AgRelationship) last;
        return new PathDescriptor(relationship.getTargetEntity().getType(), new ASTObjPath(agPath), false);
    }

    Object lastPathComponent(AgEntity<?> entity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0 || dot == path.length() - 1) {
            throw AgException.badRequest("Invalid path '%s' for '%s'", path, entity.getName());
        }

        if (dot > 0) {
            String segment = toRelationshipName(path.substring(0, dot));

            // must be a relationship ..
            AgRelationship relationship = entity.getRelationship(segment);
            if (relationship == null) {
                throw AgException.badRequest("Invalid path '%s' for '%s'. Not a relationship",
                        path,
                        entity.getName());
            }

            AgEntity<?> targetEntity = relationship.getTargetEntity();
            return lastPathComponent(targetEntity, path.substring(dot + 1));
        }

        AgAttribute attribute = entity.getAttribute(path);
        if (attribute != null) {
            return attribute;
        }

        AgIdPart idPart = entity.getIdPart(path);
        if (idPart != null) {
            return idPart;
        }

        AgRelationship relationship = entity.getRelationship(toRelationshipName(path));
        if (relationship != null) {
            return relationship;
        }

        throw AgException.badRequest("Invalid path '%s' for '%s'", path, entity.getName());
    }

    private String toRelationshipName(String pathSegment) {
        return pathSegment.endsWith("+") ? pathSegment.substring(0, pathSegment.length() - 1) : pathSegment;
    }
}
