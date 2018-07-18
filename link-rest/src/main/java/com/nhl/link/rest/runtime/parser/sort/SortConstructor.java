package com.nhl.link.rest.runtime.parser.sort;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.query.Dir;
import com.nhl.link.rest.runtime.query.Sort;
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
        processSortObject(resourceEntity, sort);
        // processes nested sorts
        if (sort != null) {
            sort.getSorts().stream().forEach(s -> processSortObject(resourceEntity, s));
        }
    }

    private void processSortObject(ResourceEntity<?> resourceEntity, Sort sort) {
        if (sort == null) {
            return;
        }

        // TODO: do we need to support nested ID?
        LrEntity<?> entity = resourceEntity.getLrEntity();

        String property = sort.getProperty();
        if (property == null || property.isEmpty()) {
            return;
        }

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

        SortOrder so = direction.equals(Dir.ASC) ? SortOrder.ASCENDING : SortOrder.DESCENDING;

        resourceEntity.getOrderings().add(new Ordering(property, so));
    }
}
