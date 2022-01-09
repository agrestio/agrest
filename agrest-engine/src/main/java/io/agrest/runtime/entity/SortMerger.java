package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
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
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings) {
        orderings.forEach(o -> collectOrdering(resourceEntity, o));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort ordering) {

        // check for dupes...
        for (Sort o : resourceEntity.getOrderings()) {
            if (o.equals(ordering)) {
                return;
            }
        }

        resourceEntity.getOrderings().add(ordering);
    }
}
