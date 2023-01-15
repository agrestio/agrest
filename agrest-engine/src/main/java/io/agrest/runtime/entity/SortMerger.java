package io.agrest.runtime.entity;

import io.agrest.ResourceEntity;
import io.agrest.access.PathChecker;
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
    public void merge(ResourceEntity<?> resourceEntity, List<Sort> orderings, PathChecker pathChecker) {
        orderings.forEach(o -> collectOrdering(resourceEntity, o, pathChecker));
    }

    private void collectOrdering(ResourceEntity<?> resourceEntity, Sort ordering, PathChecker pathChecker) {

        // check for dupes...
        for (Sort o : resourceEntity.getOrderings()) {
            if (o.equals(ordering)) {
                return;
            }
        }

        pathChecker.exceedsDepth(ordering.getPath());
        resourceEntity.getOrderings().add(ordering);
    }
}
