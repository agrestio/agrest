package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.EntityProperty;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.protocol.Include;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;


public class IncludeMerger implements IIncludeMerger {

    private ISortMerger sortMerger;
    private ICayenneExpMerger expMerger;
    private IMapByMerger mapByMerger;
    private ISizeMerger sizeMerger;

    public IncludeMerger(
            @Inject ICayenneExpMerger expMerger,
            @Inject ISortMerger sortMerger,
            @Inject IMapByMerger mapByMerger,
            @Inject ISizeMerger sizeMerger) {

        this.sortMerger = sortMerger;
        this.expMerger = expMerger;
        this.mapByMerger = mapByMerger;
        this.sizeMerger = sizeMerger;
    }

    /**
     * Records include path, returning null for the path corresponding to an
     * attribute, and a child {@link ResourceEntity} for the path corresponding
     * to relationship.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static ResourceEntity<?> processIncludePath(ResourceEntity<?> parent, String path) {
        int dot = path.indexOf(PathConstants.DOT);

        if (dot == 0) {
            throw new AgException(Status.BAD_REQUEST, "Include starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgException(Status.BAD_REQUEST, "Include ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> agEntity = parent.getAgEntity();

        if (dot < 0) {
            EntityProperty requestProperty = parent.getExtraProperties().get(property);
            if (requestProperty != null) {
                parent.getIncludedExtraProperties().put(property, requestProperty);
                return null;
            }

            AgAttribute attribute = agEntity.getAttribute(property);
            if (attribute != null) {
                parent.getAttributes().put(property, attribute);
                return null;
            }
        }

        AgRelationship relationship = agEntity.getRelationship(property);
        if (relationship != null) {

            ResourceEntity<?> childEntity = parent
                    .getChildren()
                    .computeIfAbsent(property, p -> new ResourceEntity(relationship.getTargetEntity(), relationship));

            if (dot > 0) {
                return processIncludePath(childEntity, path.substring(dot + 1));
            } else {
                processDefaultIncludes(childEntity);
                // Id should be included implicitly
                childEntity.includeId();
                return childEntity;
            }
        }

        // this is root entity id and it's included explicitly
        if (property.equals(PathConstants.ID_PK_ATTRIBUTE)) {
            parent.includeId();
            return null;
        }

        throw new AgException(Status.BAD_REQUEST, "Invalid include path: " + path);
    }

    public static void processDefaultIncludes(ResourceEntity<?> resourceEntity) {

        // either there are no includes (taking into account Id) or all includes are relationships
        if (!resourceEntity.isIdIncluded()
                && resourceEntity.getAttributes().isEmpty()
                && resourceEntity.getIncludedExtraProperties().isEmpty()) {

            for (AgAttribute a : resourceEntity.getAgEntity().getAttributes()) {
                resourceEntity.getAttributes().put(a.getName(), a);
                resourceEntity.getDefaultProperties().add(a.getName());
            }

            resourceEntity.getIncludedExtraProperties().putAll(resourceEntity.getExtraProperties());
            resourceEntity.getDefaultProperties().addAll(resourceEntity.getExtraProperties().keySet());

            // Id should be included by default
            resourceEntity.includeId();
        }
    }

    /**
     * Sanity check. We don't want to get a stack overflow.
     */
    public static void checkTooLong(String path) {
        if (path != null && path.length() > PathConstants.MAX_PATH_LENGTH) {
            throw new AgException(Response.Status.BAD_REQUEST, "Include/exclude path too long: " + path);
        }
    }

    /**
     * @since 2.13
     */
    @Override
    public void merge(ResourceEntity<?> entity, List<Include> includes) {
        for (Include include : includes) {
            process(entity, include);
        }

        IncludeMerger.processDefaultIncludes(entity);
    }

    private void process(ResourceEntity<?> entity, Include include) {
        ResourceEntity<?> includeEntity;

        String value = include.getValue();
        if (value != null && !value.isEmpty()) {
            IncludeMerger.checkTooLong(value);
            IncludeMerger.processIncludePath(entity, value);
        }

        String path = include.getPath();
        if (path == null || path.isEmpty()) {
            // root node
            includeEntity = entity;
        } else {
            IncludeMerger.checkTooLong(path);
            includeEntity = IncludeMerger.processIncludePath(entity, path);
            if (includeEntity == null) {
                throw new AgException(Status.BAD_REQUEST,
                        "Bad include spec, non-relationship 'path' in include object: " + path);
            }
        }

        mapByMerger.mergeIncluded(includeEntity, include.getMapBy());
        sortMerger.merge(includeEntity, include.getSort());
        expMerger.merge(includeEntity, include.getCayenneExp());
        sizeMerger.merge(includeEntity, include.getStart(), include.getLimit());
    }
}
