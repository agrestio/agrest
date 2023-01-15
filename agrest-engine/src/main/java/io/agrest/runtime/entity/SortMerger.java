package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.MaxPathDepth;
import io.agrest.protocol.Sort;

import java.util.List;

/**
 * @since 2.13
 */
public class SortMerger implements ISortMerger {

    /**
     * @since 2.13
     */
    @Override
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings, MaxPathDepth maxPathDepth) {
        orderings.forEach(o -> collectOrdering(resourceEntity, o, maxPathDepth));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort ordering, MaxPathDepth maxPathDepth) {

        // check for dupes...
        for (Sort o : resourceEntity.getOrderings()) {
            if (o.equals(ordering)) {
                return;
            }
        }

        maxPathDepth.checkExceedsDepth(ordering.getPath());
        resourceEntity.getOrderings().add(ordering);
    }
}
