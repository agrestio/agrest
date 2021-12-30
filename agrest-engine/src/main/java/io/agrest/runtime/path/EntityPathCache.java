package io.agrest.runtime.path;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class EntityPathCache {

    private final AgEntity<?> entity;
    private final Map<String, PathDescriptor> pathCache;

    EntityPathCache(AgEntity<?> entity) {
        this.entity = entity;
        this.pathCache = new ConcurrentHashMap<>();

        // immediately cache a special entry matching "id" constant ... if there
        // is a single ID

        // TODO: single ID check allows us to support id-less entities (quite
        // common, e.g. various aggregated data reports). However it does not
        // solve an issue of more common case of entities with multi-column ID.
        // Will need a concept of "virtual" ID built from ObjectId (or POJO id
        // properties) via cayenne-lifecycle.

        if (entity.getIdParts().size() == 1) {

            pathCache.put(PathConstants.ID_PK_ATTRIBUTE, new PathDescriptor() {

                AgIdPart id = entity.getIdParts().iterator().next();

                @Override
                public boolean isAttribute() {
                    return true;
                }

                @Override
                public Class<?> getType() {
                    return id.getType();
                }

                @Override
                public ASTPath getPathExp() {
                    return id.getPathExp();
                }
            });
        }
    }

    PathDescriptor getPathDescriptor(ASTObjPath path) {
        return pathCache.computeIfAbsent(path.getPath(), p -> computePathDescriptor(path));
    }

    private PathDescriptor computePathDescriptor(ASTObjPath path) {

        String stringPath = (String) path.getOperand(0);
        final Object last = lastPathComponent(entity, stringPath);

        if (last instanceof AgAttribute) {
            return new PathDescriptor() {

                AgAttribute attribute = (AgAttribute) last;

                @Override
                public boolean isAttribute() {
                    return true;
                }

                @Override
                public Class<?> getType() {
                    return attribute.getType();
                }

                @Override
                public ASTPath getPathExp() {
                    return path;
                }
            };
        } else {
            return new PathDescriptor() {

                AgRelationship relationship = (AgRelationship) last;
                Class<?> type = relationship.getTargetEntity().getType();

                @Override
                public boolean isAttribute() {
                    return false;
                }

                @Override
                public Class<?> getType() {
                    return type;
                }

                @Override
                public ASTPath getPathExp() {
                    return path;
                }
            };
        }
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

        // can be a relationship or an attribute
        AgAttribute attribute = entity.getAttribute(path);
        if (attribute != null) {
            return attribute;
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
