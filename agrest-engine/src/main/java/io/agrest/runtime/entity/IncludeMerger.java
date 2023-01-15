package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.ResourceEntity;
import io.agrest.ResourceEntityProjection;
import io.agrest.protocol.Include;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntityOverlay;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;


public class IncludeMerger implements IIncludeMerger {

    protected AgSchema schema;
    protected ISortMerger sortMerger;
    protected IExpMerger expMerger;
    protected IMapByMerger mapByMerger;
    protected ISizeMerger sizeMerger;

    public IncludeMerger(
            @Inject AgSchema schema,
            @Inject IExpMerger expMerger,
            @Inject ISortMerger sortMerger,
            @Inject IMapByMerger mapByMerger,
            @Inject ISizeMerger sizeMerger) {

        this.schema = schema;
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
            throw AgException.badRequest("Include/exclude path too long: %s", path);
        }
    }

    /**
     * @since 3.4
     */
    @Override
    public void merge(ResourceEntity<?> entity, List<Include> includes, Map<Class<?>, AgEntityOverlay<?>> overlays, int maxPathDepth) {

        // included attribute sets of the root entity and entities that are included explicitly via relationship includes
        // may need to get expanded if they don't have any explicit includes otherwise. Will track them here... Entities
        // that are NOT expanded are those that are "phantom" entities included as a part of the longer path.

        PhantomTrackingResourceEntityTreeBuilder treeBuilder
                = new PhantomTrackingResourceEntityTreeBuilder(entity, schema, overlays);

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
            Map<Class<?>, AgEntityOverlay<?>> overlays) {

        String path = include.getPath();
        ResourceEntity<?> includeEntity = (path == null || path.isEmpty()) ? entity : treeBuilder.inflatePath(path);

        mapByMerger.merge(includeEntity, include.getMapBy(), overlays);
        sortMerger.merge(includeEntity, include.getSorts());
        expMerger.merge(includeEntity, include.getExp());
        sizeMerger.merge(includeEntity, include.getStart(), include.getLimit());
    }

    private void processDefaultIncludes(ResourceEntity<?> resourceEntity) {

        // TODO: will need to take different projections into account
        if (!resourceEntity.isIdIncluded() && resourceEntity.getBaseProjection().getAttributes().isEmpty()) {

            for(ResourceEntityProjection<?> p : resourceEntity.getProjections()) {
                for (AgAttribute a : p.getAgEntity().getAttributes()) {
                    p.ensureAttribute(a.getName(), true);
                }
            }

            resourceEntity.includeId();
        }
    }
}
