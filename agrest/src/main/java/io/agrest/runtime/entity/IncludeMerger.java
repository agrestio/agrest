package io.agrest.runtime.entity;

import io.agrest.AgRESTException;
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

    private ISortMerger sortConstructor;
    private ICayenneExpMerger expConstructor;
    private IMapByMerger mapByConstructor;
    private ISizeMerger sizeConstructor;

    public IncludeMerger(
            @Inject ICayenneExpMerger expConstructor,
            @Inject ISortMerger sortConstructor,
            @Inject IMapByMerger mapByConstructor,
            @Inject ISizeMerger sizeConstructor) {

        this.sortConstructor = sortConstructor;
        this.expConstructor = expConstructor;
        this.mapByConstructor = mapByConstructor;
        this.sizeConstructor = sizeConstructor;
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
            throw new AgRESTException(Status.BAD_REQUEST, "Include starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new AgRESTException(Status.BAD_REQUEST, "Include ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        AgEntity<?> agEntity = parent.getAgEntity();
        AgAttribute attribute = agEntity.getAttribute(property);
        if (attribute != null) {

            if (dot > 0) {
                throw new AgRESTException(Status.BAD_REQUEST, "Invalid include path: " + path);
            }

            parent.getAttributes().put(property, attribute);
            return null;
        }

        AgRelationship relationship = agEntity.getRelationship(property);
        if (relationship != null) {

            ResourceEntity<?> childEntity = parent.getChild(property);
            if (childEntity == null) {
                AgEntity<?> targetType = relationship.getTargetEntity();
                childEntity = new ResourceEntity(targetType, relationship);
                parent.getChildren().put(property, childEntity);
            }

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

        throw new AgRESTException(Status.BAD_REQUEST, "Invalid include path: " + path);
    }

    public static void processDefaultIncludes(ResourceEntity<?> resourceEntity) {
        // either there are no includes (taking into account Id) or all includes
        // are relationships
        if (!resourceEntity.isIdIncluded() && resourceEntity.getAttributes().isEmpty()) {

            for (AgAttribute a : resourceEntity.getAgEntity().getAttributes()) {
                resourceEntity.getAttributes().put(a.getName(), a);
                resourceEntity.getDefaultProperties().add(a.getName());
            }

            // Id should be included by default
            resourceEntity.includeId();
        }
    }

    /**
     * Sanity check. We don't want to get a stack overflow.
     */
    public static void checkTooLong(String path) {
        if (path != null && path.length() > PathConstants.MAX_PATH_LENGTH) {
            throw new AgRESTException(Response.Status.BAD_REQUEST, "Include/exclude path too long: " + path);
        }
    }

    /**
     * @since 2.13
     */
    @Override
    public void merge(ResourceEntity<?> resourceEntity, List<Include> includes) {
        for (Include include : includes) {
            processOne(resourceEntity, include);
        }

        IncludeMerger.processDefaultIncludes(resourceEntity);
    }

    private void processOne(ResourceEntity<?> resourceEntity, Include include) {
        processIncludeObject(resourceEntity, include);
        // processes nested includes
        if (include != null) {
            include.getIncludes().forEach(i -> processIncludeObject(resourceEntity, i));
        }
    }

    private void processIncludeObject(ResourceEntity<?> rootEntity, Include include) {
        if (include != null) {
            ResourceEntity<?> includeEntity;

            final String value = include.getValue();
            if (value != null && !value.isEmpty()) {
                IncludeMerger.checkTooLong(value);
                IncludeMerger.processIncludePath(rootEntity, value);
            }

            final String path = include.getPath();
            if (path == null || path.isEmpty()) {
                // root node
                includeEntity = rootEntity;
            } else {
                IncludeMerger.checkTooLong(path);
                includeEntity = IncludeMerger.processIncludePath(rootEntity, path);
                if (includeEntity == null) {
                    throw new AgRESTException(Status.BAD_REQUEST,
                            "Bad include spec, non-relationship 'path' in include object: " + path);
                }
            }

            mapByConstructor.mergeIncluded(includeEntity, include.getMapBy());
            sortConstructor.merge(includeEntity, include.getSort());
            expConstructor.merge(includeEntity, include.getCayenneExp());
            sizeConstructor.merge(includeEntity, include.getStart(), include.getLimit());
        }
    }
}
