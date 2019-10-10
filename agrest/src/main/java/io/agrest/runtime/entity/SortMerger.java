package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.path.IPathDescriptorManager;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

import java.util.List;

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
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings) {
        orderings.forEach(o -> collectOrdering(resourceEntity, o));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort ordering) {

        String property = ordering.getProperty();
        if (property != null && !property.isEmpty()) {

            // TODO: do we need to support nested ID?
            AgEntity<?> entity = resourceEntity.getAgEntity();

            // note using "toString" instead of "getPath" to convert ASTPath to  String representation. This ensures
            // "db:" prefix is preserved if present
            property = pathCache.getPathDescriptor(entity, new ASTObjPath(ordering.getProperty())).getPathExp().toString();

            // check for dupes...
            for (Ordering o : resourceEntity.getOrderings()) {
                if (property.equals(o.getSortSpecString())) {
                    return;
                }
            }


            SortOrder so = directionToSortOrder(ordering.getDirection());
            resourceEntity.getOrderings().add(new Ordering(property, so));
        }
    }

    private SortOrder directionToSortOrder(Dir direction) {
        switch (direction) {
            case ASC:
                return SortOrder.ASCENDING;
            case ASC_CI:
                return SortOrder.ASCENDING_INSENSITIVE;
            case DESC_CI:
                return SortOrder.DESCENDING_INSENSITIVE;
            case DESC:
                return SortOrder.DESCENDING;
            default:
                throw new IllegalArgumentException("Missing or unexpected sort direction: " + direction);
        }
    }

}
