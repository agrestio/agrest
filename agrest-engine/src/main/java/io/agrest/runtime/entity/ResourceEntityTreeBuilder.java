package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.NestedResourceEntity;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.meta.*;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Assembles {@link ResourceEntity} tree from a set of paths.
 *
 * @since 3.4
 */
public class ResourceEntityTreeBuilder {

    private final ResourceEntity<?> rootEntity;
    private final AgDataMap agDataMap;
    private final Map<Class<?>, AgEntityOverlay<?>> entityOverlays;

    public ResourceEntityTreeBuilder(
            ResourceEntity<?> rootEntity,
            AgDataMap agDataMap,
            Map<Class<?>, AgEntityOverlay<?>> entityOverlays) {

        this.agDataMap = Objects.requireNonNull(agDataMap);
        this.rootEntity = Objects.requireNonNull(rootEntity);
        this.entityOverlays = entityOverlays != null ? entityOverlays : Collections.emptyMap();
    }

    /**
     * Updates the internal {@link ResourceEntity} adding related entities and and attributes to match the path.
     *
     * @param path property path
     * @return the last entity found in the path
     */
    public ResourceEntity<?> inflatePath(String path) {
        IncludeMerger.checkTooLong(path);
        return doInflatePath(rootEntity, path);
    }

    /**
     * Records include path, returning null for the path corresponding to an attribute, and a child
     * {@link ResourceEntity} for the path corresponding to relationship.
     */
    protected ResourceEntity<?> doInflatePath(ResourceEntity<?> entity, String path) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Response.Status.BAD_REQUEST, "Include starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Response.Status.BAD_REQUEST, "Include ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> agEntity = entity.getAgEntity();
        AgEntityOverlay<?> agEntityOverlay = entity.getAgEntityOverlay();

        if (dot < 0) {

            AgAttribute attribute = agEntity.getAttribute(property);
            AgAttributeOverlay attributeOverlay = agEntityOverlay != null ? agEntityOverlay.getAttributeOverlay(property) : null;
            if (attributeOverlay != null) {
                AgAttribute overlaid = attributeOverlay.resolve(attribute);
                entity.addAttribute(overlaid, false);
                return entity;
            }

            if (attribute != null) {
                entity.addAttribute(attribute, false);
                return entity;
            }
        }

        AgRelationship relationship = agEntity.getRelationship(property);
        AgRelationshipOverlay relationshipOverlay = agEntityOverlay != null ? agEntityOverlay.getRelationshipOverlay(property) : null;
        if (relationshipOverlay != null) {
            AgRelationship overlaid = relationshipOverlay.resolve(relationship, agDataMap);
            String childPath = dot > 0 ? path.substring(dot + 1) : null;
            return inflateChild(entity, overlaid, childPath);
        }

        if (relationship != null) {
            String childPath = dot > 0 ? path.substring(dot + 1) : null;
            return inflateChild(entity, relationship, childPath);
        }

        // this is root entity id and it's included explicitly
        if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            entity.includeId();
            return entity;
        }

        throw new AgException(Response.Status.BAD_REQUEST, "Invalid include path: " + path);
    }

    protected ResourceEntity<?> inflateChild(ResourceEntity<?> parentEntity, AgRelationship relationship, String childPath) {
        ResourceEntity<?> childEntity = parentEntity
                .getChildren()
                .computeIfAbsent(relationship.getName(), p -> createChildEntity(parentEntity, relationship));

        return childPath != null
                ? doInflatePath(childEntity, childPath)
                : childEntity;
    }

    protected NestedResourceEntity<?> createChildEntity(ResourceEntity<?> parent, AgRelationship incoming) {
        AgEntity<?> target = incoming.getTargetEntity();
        return new NestedResourceEntity(target, entityOverlays.get(target.getType()), parent, incoming);
    }
}
