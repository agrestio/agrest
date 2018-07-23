package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.PathConstants;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;


public class IncludeConstructor implements IIncludeConstructor {

    private ISortConstructor sortConstructor;
    private ICayenneExpConstructor expConstructor;
    private IMapByConstructor mapByConstructor;
    private ISizeConstructor sizeConstructor;

    public IncludeConstructor(
            @Inject ICayenneExpConstructor expConstructor,
            @Inject ISortConstructor sortConstructor,
            @Inject IMapByConstructor mapByConstructor,
            @Inject ISizeConstructor sizeConstructor) {

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
            throw new LinkRestException(Status.BAD_REQUEST, "Include starts with dot: " + path);
        }

        if (dot == path.length() - 1) {
            throw new LinkRestException(Status.BAD_REQUEST, "Include ends with dot: " + path);
        }

        String property = dot > 0 ? path.substring(0, dot) : path;
        LrEntity<?> lrEntity = parent.getLrEntity();
        LrAttribute attribute = lrEntity.getAttribute(property);
        if (attribute != null) {

            if (dot > 0) {
                throw new LinkRestException(Status.BAD_REQUEST, "Invalid include path: " + path);
            }

            parent.getAttributes().put(property, attribute);
            return null;
        }

        LrRelationship relationship = lrEntity.getRelationship(property);
        if (relationship != null) {

            ResourceEntity<?> childEntity = parent.getChild(property);
            if (childEntity == null) {
                LrEntity<?> targetType = relationship.getTargetEntity();
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

        throw new LinkRestException(Status.BAD_REQUEST, "Invalid include path: " + path);
    }

    public static void processDefaultIncludes(ResourceEntity<?> resourceEntity) {
        // either there are no includes (taking into account Id) or all includes
        // are relationships
        if (!resourceEntity.isIdIncluded() && resourceEntity.getAttributes().isEmpty()) {

            for (LrAttribute a : resourceEntity.getLrEntity().getAttributes()) {
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
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Include/exclude path too long: " + path);
        }
    }

    /**
     * @since 2.13
     */
    @Override
    public void construct(ResourceEntity<?> resourceEntity, List<Include> includes) {
        for (Include include : includes) {
            processOne(resourceEntity, include);
        }

        IncludeConstructor.processDefaultIncludes(resourceEntity);
    }

    private void processOne(ResourceEntity<?> resourceEntity, Include include) {
        processIncludeObject(resourceEntity, include);
        // processes nested includes
        if (include != null) {
            include.getIncludes().stream().forEach(i -> processIncludeObject(resourceEntity, i));
        }
    }

    private void processIncludeObject(ResourceEntity<?> rootEntity, Include include) {
        if (include != null) {
            ResourceEntity<?> includeEntity;

            final String value = include.getValue();
            if (value != null && !value.isEmpty()) {
                IncludeConstructor.checkTooLong(value);
                IncludeConstructor.processIncludePath(rootEntity, value);
            }

            final String path = include.getPath();
            if (path == null || path.isEmpty()) {
                // root node
                includeEntity = rootEntity;
            } else {
                IncludeConstructor.checkTooLong(path);
                includeEntity = IncludeConstructor.processIncludePath(rootEntity, path);
                if (includeEntity == null) {
                    throw new LinkRestException(Status.BAD_REQUEST,
                            "Bad include spec, non-relationship 'path' in include object: " + path);
                }
            }

            mapByConstructor.constructIncluded(includeEntity, include.getMapBy());
            sortConstructor.construct(includeEntity, include.getSort());
            expConstructor.construct(includeEntity, include.getCayenneExp());
            sizeConstructor.construct(includeEntity, include.getStart(), include.getLimit());
        }
    }
}
