package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.RelatedResourceEntity;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.meta.*;

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
    private final AgSchema schema;
    private final Map<Class<?>, AgEntityOverlay<?>> entityOverlays;

    public ResourceEntityTreeBuilder(
            ResourceEntity<?> rootEntity,
            AgSchema schema,
            Map<Class<?>, AgEntityOverlay<?>> entityOverlays) {

        this.schema = Objects.requireNonNull(schema);
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
            throw AgException.badRequest("Include starts with dot: %s", path);
        }

        if (dot == path.length() - 1) {
            throw AgException.badRequest("Include ends with dot: %s", path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> agEntity = entity.getAgEntity();

        if (dot < 0) {

            AgAttribute attribute = agEntity.getAttributeInHierarchy(property);
            if (attribute != null) {
                entity.addAttribute(attribute, false);
                return entity;
            }
        }

        AgRelationship relationship = agEntity.getRelationship(property);

        if (relationship != null) {
            String childPath = dot > 0 ? path.substring(dot + 1) : null;
            return inflateChild(entity, relationship, childPath);
        }

        // this is root entity id and it's included explicitly
        if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            entity.includeId();
            return entity;
        }

        throw AgException.badRequest("Invalid include path: %s", path);
    }

    protected ResourceEntity<?> inflateChild(ResourceEntity<?> parentEntity, AgRelationship relationship, String childPath) {
        ResourceEntity<?> childEntity = parentEntity
                .getChildren()
                .computeIfAbsent(relationship.getName(), p -> createChildEntity(parentEntity, relationship));

        return childPath != null
                ? doInflatePath(childEntity, childPath)
                : childEntity;
    }

    protected RelatedResourceEntity<?> createChildEntity(ResourceEntity<?> parent, AgRelationship incoming) {

        // TODO: If the target is overlaid, we need to overlay the "incoming" relationship as well for model consistency...
        //  Currently we optimistically assume that no request processing code would rely on "incoming.target"
        AgEntity<?> target = incoming.getTargetEntity();
        AgEntityOverlay targetOverlay = entityOverlays.get(target.getType());
        AgEntity<?> overlaidTarget = targetOverlay != null ? targetOverlay.resolve(schema, target) : target;

        return incoming.isToMany()
                ? new ToManyResourceEntity<>(overlaidTarget, parent, incoming)
                : new ToOneResourceEntity<>(overlaidTarget, parent, incoming);
    }
}
