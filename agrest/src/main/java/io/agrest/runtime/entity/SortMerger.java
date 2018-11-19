package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.path.IPathDescriptorManager;
import org.apache.cayenne.di.Inject;
import io.agrest.backend.exp.parser.ASTObjPath;
import io.agrest.backend.query.Ordering;
import io.agrest.backend.query.SortOrder;

/**
 * @since 2.13
 */
public class SortMerger implements ISortMerger {

    private IPathDescriptorManager pathCache;

    public SortMerger(@Inject IPathDescriptorManager pathCache) {
        this.pathCache = pathCache;
    }

    /**
     * @since 2.13
     */
    @Override
    public void merge(ResourceEntity<?> resourceEntity, Sort sort) {
        if(sort != null) {
            collectOrderings(resourceEntity, sort);
        }
    }

    private void collectOrderings(ResourceEntity<?> resourceEntity, Sort sort) {
        collectOrdering(resourceEntity, sort);
        sort.getSorts().forEach(s -> collectOrderings(resourceEntity, s));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort sort) {

        String property = sort.getProperty();
        if (property != null && !property.isEmpty()) {

            // TODO: do we need to support nested ID?
            AgEntity<?> entity = resourceEntity.getAgEntity();

            // note using "toString" instead of "getPath" to convert ASTPath to
            // String representation. This ensures "db:" prefix is preserved if
            // present
            property = pathCache.getPathDescriptor(entity, new ASTObjPath(sort.getProperty())).getPathExp().toString();

            // check for dupes...
            for (Ordering o : resourceEntity.getOrderings()) {
                if (property.equals(o.getSortSpecString())) {
                    return;
                }
            }

            Dir direction = sort.getDirection();
            if (direction == null) {
                direction = Dir.ASC;
            }

            SortOrder so = direction == Dir.ASC ? SortOrder.ASCENDING : SortOrder.DESCENDING;
            resourceEntity.getOrderings().add(new Ordering(property, so));
        }
    }
}
