package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.protocol.Include;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;


public class IncludeMerger implements IIncludeMerger {

    protected IMetadataService metadataService;
    protected ISortMerger sortMerger;
    protected ICayenneExpMerger expMerger;
    protected IMapByMerger mapByMerger;
    protected ISizeMerger sizeMerger;

    public IncludeMerger(
            @Inject IMetadataService metadataService,
            @Inject ICayenneExpMerger expMerger,
            @Inject ISortMerger sortMerger,
            @Inject IMapByMerger mapByMerger,
            @Inject ISizeMerger sizeMerger) {

        this.metadataService = metadataService;
        this.sortMerger = sortMerger;
        this.expMerger = expMerger;
        this.mapByMerger = mapByMerger;
        this.sizeMerger = sizeMerger;
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
     * @since 3.4
     */
    @Override
    public void merge(ResourceEntity<?> entity, List<Include> includes, Map<String, AgEntityOverlay<?>> overlays) {

        // included attribute sets of the root entity and entities that are included explicitly via relationship includes
        // may need to get expanded if they don't have any explicit includes otherwise. Will track them here... Entities
        // that are NOT expanded are those that are "phantom" entities included as a part of the longer path.

        PhantomTrackingResourceEntityTreeBuilder treeBuilder
                = new PhantomTrackingResourceEntityTreeBuilder(entity, metadataService::getAgEntity, overlays);

        for (Include include : includes) {
            mergeInclude(entity, include, treeBuilder, overlays);
        }

        for (ResourceEntity<?> e : treeBuilder.nonPhantomEntities()) {
            processDefaultIncludes(e);
        }
    }

    private void mergeInclude(
            ResourceEntity<?> entity,
            Include include,
            ResourceEntityTreeBuilder treeBuilder,
            Map<String, AgEntityOverlay<?>> overlays) {

        String path = include.getPath();
        ResourceEntity<?> includeEntity = (path == null || path.isEmpty()) ? entity : treeBuilder.inflatePath(path);

        mapByMerger.mergeIncluded(includeEntity, include.getMapBy(), overlays);
        sortMerger.merge(includeEntity, include.getOrderings());
        expMerger.merge(includeEntity, include.getCayenneExp());
        sizeMerger.merge(includeEntity, include.getStart(), include.getLimit());
    }

    private void processDefaultIncludes(ResourceEntity<?> resourceEntity) {
        if (!resourceEntity.isIdIncluded()
                && resourceEntity.getAttributes().isEmpty()
                && resourceEntity.getIncludedExtraProperties().isEmpty()) {

            for (AgAttribute a : resourceEntity.getAgEntity().getAttributes()) {
                resourceEntity.getAttributes().put(a.getName(), a);
                resourceEntity.getDefaultProperties().add(a.getName());
            }

            if (resourceEntity.getAgEntityOverlay() != null) {
                for (AgAttribute a : resourceEntity.getAgEntityOverlay().getAttributes()) {
                    resourceEntity.getAttributes().put(a.getName(), a);
                    resourceEntity.getDefaultProperties().add(a.getName());
                }
            }

            resourceEntity.getIncludedExtraProperties().putAll(resourceEntity.getExtraProperties());
            resourceEntity.getDefaultProperties().addAll(resourceEntity.getExtraProperties().keySet());
            resourceEntity.includeId();
        }
    }
}
