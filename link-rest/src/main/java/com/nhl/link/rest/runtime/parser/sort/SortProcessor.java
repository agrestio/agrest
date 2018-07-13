package com.nhl.link.rest.runtime.parser.sort;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.cache.IPathCache;
import com.nhl.link.rest.runtime.query.Query;
import com.nhl.link.rest.runtime.query.Sort;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.SortOrder;

import javax.ws.rs.core.Response;

/**
 * @since 1.5
 */
public class SortProcessor implements ISortProcessor {

    private static final String ASC = "ASC";
    private static final String DESC = "DESC";

    private SortConverter converter;
    private IPathCache pathCache;

    public SortProcessor(@Inject IJacksonService jacksonService, @Inject IPathCache pathCache) {
        this.converter = new SortConverter(jacksonService);
        this.pathCache = pathCache;
    }

    @Override
    public void process(ResourceEntity<?> resourceEntity, String value, String direction) {
        Sort sort = converter.fromString(value);
        if (sort != null && direction != null) {
            sort.setDirection(direction);
        }

        processSortObject(resourceEntity, sort);

        // processes nested sorts
        if (sort != null) {
            sort.getSorts().stream().forEach(s -> processSortObject(resourceEntity, s));
        }
    }

    /**
     * @since 2.13
     */
    @Override
    public void process(ResourceEntity<?> resourceEntity, Query query) {
        processSortObject(resourceEntity, query.getSort());
        // processes nested sorts
        if (query.getSort() != null) {
            query.getSort().getSorts().stream().forEach(s -> processSortObject(resourceEntity, s));
        }
    }

    /**
     * @since 2.13
     */
    @Override
    public SortConverter getConverter() {
        return this.converter;
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

        String direction = sort.getDirection();
        if (direction == null || direction.isEmpty()) {
            direction = ASC;
        } else {
            checkInvalidDirection(direction);
        }

        SortOrder so = direction.equals(ASC) ? SortOrder.ASCENDING : SortOrder.DESCENDING;

        resourceEntity.getOrderings().add(new Ordering(property, so));
    }

    private static void checkInvalidDirection(String direction) {
        if (!(ASC.equals(direction) || DESC.equals(direction))) {
            throw new LinkRestException(Response.Status.BAD_REQUEST, "Direction is invalid: " + direction);
        }
    }
}
