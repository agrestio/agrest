package com.nhl.link.rest.runtime.parser.sort;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

/**
 * @since 2.13
 */
public class SortConstructor implements ISortConstructor {

    private IPathCache pathCache;

    public SortConstructor(@Inject IPathCache pathCache) {
        this.pathCache = pathCache;
    }

    /**
     * @since 2.13
     */
    @Override
    public void construct(ResourceEntity<?> resourceEntity, Sort sort) {
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
            LrEntity<?> entity = resourceEntity.getLrEntity();

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
