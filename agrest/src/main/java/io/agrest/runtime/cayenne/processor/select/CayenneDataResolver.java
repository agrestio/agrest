package io.agrest.runtime.cayenne.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.query.SelectQuery;

import java.util.List;

/**
 * @since 3.4
 */
public abstract class CayenneDataResolver {

    protected CayenneQueryAssembler queryAssembler;
    protected ICayennePersister persister;

    public CayenneDataResolver(CayenneQueryAssembler queryAssembler, ICayennePersister persister) {
        this.queryAssembler = queryAssembler;
        this.persister = persister;
    }

    protected void addPrefetch(NestedResourceEntity<?> entity) {
        addPrefetch(entity, null);
    }

    protected void addPrefetch(NestedResourceEntity<?> entity, String outgoingPath) {

        // add prefetch to the first available (grand-)parent query

        String incomingPath = entity.getIncoming().getName();
        String path = outgoingPath != null ? incomingPath + "." + outgoingPath : incomingPath;

        ResourceEntity<?> parent = entity.getParent();
        if (parent.getSelect() != null) {
            parent.getSelect().addPrefetch(path);
            return;
        }

        if (parent instanceof RootResourceEntity) {
            throw new IllegalStateException(
                    "Can't add prefetch to root entity that has no SelectQuery of its own. Path: " + path);
        }

        addPrefetch(((NestedResourceEntity) parent), path);
    }

    protected <T> List<T> fetch(ResourceEntity<T> entity) {
        return persister.sharedContext().select(entity.getSelect());
    }

    protected void afterQueryAssembled(ResourceEntity<?> entity, SelectContext<?> context) {

        ResourceEntity<?> mapBy = entity.getMapBy();
        if (mapBy != null) {

            // copy owner's query to MapBy to ensure its own resolvers do not get confused...
            mapBy.setSelect((SelectQuery) entity.getSelect());

            for (NestedResourceEntity<?> c : mapBy.getChildren().values()) {
                c.onParentQueryAssembled(context);
            }
        }

        for (NestedResourceEntity<?> c : entity.getChildren().values()) {
            c.onParentQueryAssembled(context);
        }
    }

    protected <T> void afterDataFetched(ResourceEntity<T> entity, Iterable<T> data, SelectContext<?> context) {

        ResourceEntity<?> mapBy = entity.getMapBy();
        if (mapBy != null) {
            for (NestedResourceEntity<?> c : mapBy.getChildren().values()) {
                c.onParentDataResolved(data, context);
            }
        }

        for (NestedResourceEntity<?> c : entity.getChildren().values()) {
            c.onParentDataResolved(data, context);
        }
    }
}
