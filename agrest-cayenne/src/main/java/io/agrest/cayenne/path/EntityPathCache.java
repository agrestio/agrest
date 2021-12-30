package io.agrest.cayenne.path;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.apache.cayenne.exp.parser.ASTDbPath;
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
    }

    PathDescriptor getOrCreate(String agPath) {
        return pathCache.computeIfAbsent(agPath, p -> computePathDescriptor(agPath));
    }

    private PathDescriptor computePathDescriptor(String agPath) {

        final Object last = lastPathComponent(entity, agPath);

        if (last instanceof AgAttribute) {
            return new PathDescriptor() {

                final ASTObjPath cayennePath = new ASTObjPath(agPath);
                final AgAttribute attribute = (AgAttribute) last;

                @Override
                public boolean isAttributeOrId() {
                    return true;
                }

                @Override
                public Class<?> getType() {
                    return attribute.getType();
                }

                @Override
                public ASTPath getPathExp() {
                    return cayennePath;
                }
            };
        }

        if (last instanceof AgIdPart) {
            return new PathDescriptor() {

                final AgIdPart id = (AgIdPart) last;
                final ASTPath idPath = id.getName().startsWith(ASTDbPath.DB_PREFIX)
                        ? new ASTDbPath(id.getName().substring(ASTDbPath.DB_PREFIX.length()))
                        : new ASTObjPath(id.getName());

                @Override
                public boolean isAttributeOrId() {
                    return true;
                }

                @Override
                public Class<?> getType() {
                    return id.getType();
                }

                @Override
                public ASTPath getPathExp() {
                    return idPath;
                }
            };
        }

        return new PathDescriptor() {

            final ASTObjPath cayennePath = new ASTObjPath(agPath);
            final AgRelationship relationship = (AgRelationship) last;
            final Class<?> type = relationship.getTargetEntity().getType();

            @Override
            public boolean isAttributeOrId() {
                return false;
            }

            @Override
            public Class<?> getType() {
                return type;
            }

            @Override
            public ASTPath getPathExp() {
                return cayennePath;
            }
        };
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
