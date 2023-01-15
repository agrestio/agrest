package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.ResourceEntityProjection;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Assembles {@link ResourceEntity} tree from a set of paths.
 *
 * @since 3.4
 */
public class ResourceEntityTreeBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceEntityTreeBuilder.class);

    private final ResourceEntity<?> rootEntity;
    private final AgSchema schema;
    private final Map<Class<?>, AgEntityOverlay<?>> entityOverlays;
    private final int maxTreeDepth;
    private final boolean quietTruncateLongPaths;

    public ResourceEntityTreeBuilder(
            ResourceEntity<?> rootEntity,
            AgSchema schema,
            Map<Class<?>, AgEntityOverlay<?>> entityOverlays,
            int maxTreeDepth,
            boolean quietTruncateLongPaths) {

        this.schema = Objects.requireNonNull(schema);
        this.rootEntity = Objects.requireNonNull(rootEntity);
        this.entityOverlays = entityOverlays != null ? entityOverlays : Collections.emptyMap();
        this.maxTreeDepth = maxTreeDepth;
        this.quietTruncateLongPaths = quietTruncateLongPaths;
    }

    /**
     * Updates the internal {@link ResourceEntity} adding related entities and and attributes to match the path.
     *
     * @param path property path
     * @return the last entity found in the path
     */
    public ResourceEntity<?> inflatePath(String path) {
        IncludeMerger.checkTooLong(path);
        return doInflatePath(rootEntity, path, maxTreeDepth);
    }

    /**
     * Records include path, returning null for the path corresponding to an attribute, and a child
     * {@link ResourceEntity} for the path corresponding to relationship.
     */
    protected ResourceEntity<?> doInflatePath(ResourceEntity<?> entity, String path, int remainingDepth) {

        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw AgException.badRequest("Include starts with dot: %s", path);
        }

        if (dot == path.length() - 1) {
            throw AgException.badRequest("Include ends with dot: %s", path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;

        if (dot < 0) {

            if (entity.ensureAttribute(property, false)) {
                return entity;
            } else if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
                entity.includeId();
                return entity;
            }
        }

        if (remainingDepth == 0 && entity.hasRelationship(property)) {

            if (quietTruncateLongPaths) {
                LOGGER.info(
                        "Truncated '{}' from the path as it exceeds the max allowed depth of {}",
                        path,
                        maxTreeDepth);

                return entity;
            } else {
                throw AgException.badRequest(
                        "Path exceeds the max allowed depth of %s, the remaining path '%s' can't be processed",
                        maxTreeDepth,
                        path);
            }
        }

        if (entity.ensureRelationship(property)) {
            String childPath = dot > 0 ? path.substring(dot + 1) : null;
            return inflateChild(entity, property, childPath, remainingDepth);
        }

        throw AgException.badRequest("Invalid include path: %s", path);
    }

    protected ResourceEntity<?> inflateChild(ResourceEntity<?> parentEntity, String relationshipName, String childPath, int remainingDepth) {

        ResourceEntity<?> childEntity = parentEntity.ensureChild(relationshipName, this::createChildEntity);

        return childPath != null
                ? doInflatePath(childEntity, childPath, remainingDepth - 1)
                : childEntity;
    }

    protected RelatedResourceEntity<?> createChildEntity(ResourceEntity<?> parent, String incomingName) {

        // TODO: there may be more than one incoming relationship. RelatedResourceEntity should know about all of them,
        //   not just the topmost in the inheritance hierarchy

        // TODO: If the target is overlaid, we need to overlay the "incoming" relationship as well for model consistency...
        //  Currently we optimistically assume that no request processing code would rely on "incoming.target"

        AgRelationship incoming = findFirstRelationship(parent, incomingName);
        AgEntity<?> target = incoming.getTargetEntity();
        AgEntityOverlay targetOverlay = entityOverlays.get(target.getType());
        AgEntity<?> overlaidTarget = target.resolveOverlay(schema, targetOverlay);

        return incoming.isToMany()
                ? new ToManyResourceEntity<>(overlaidTarget, parent, incoming)
                : new ToOneResourceEntity<>(overlaidTarget, parent, incoming);
    }

    // This could have been an AgEntity method, but per notes above, we should not really be doing it this way.
    // So adding public API to search for relationship in hierarchy is not desirable
    private AgRelationship findFirstRelationship(ResourceEntity<?> entity, String relationshipName) {
        for (ResourceEntityProjection<?> p : entity.getProjections()) {

            AgRelationship r = p.getRelationship(relationshipName);
            if (r != null) {
                return r;
            }
        }

        throw AgException.badRequest("No relationship named '%s' in '%s'", relationshipName, entity.getName());
    }
}
