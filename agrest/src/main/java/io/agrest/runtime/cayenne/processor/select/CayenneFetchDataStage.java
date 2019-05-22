package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @since 2.7
 */
public class CayenneFetchDataStage implements Processor<SelectContext<?>> {

    private ICayennePersister persister;

    public CayenneFetchDataStage(@Inject ICayennePersister persister) {

        // Store persister, don't extract ObjectContext from it right away.
        // Such deferred initialization may help building POJO pipelines.

        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {

        List<T> objects = fetchEntity(context.getEntity());

        if (context.isAtMostOneObject() && objects.size() != 1) {

            AgEntity<?> entity = context.getEntity().getAgEntity();

            if (objects.isEmpty()) {
                throw new AgException(Response.Status.NOT_FOUND,
                        String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            } else {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                        "Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            }
        }

        // saves a result for the root entity
        context.getEntity().setResult(objects);
    }

    protected <T> List<T>  fetchEntity(ResourceEntity<T> resourceEntity) {
        SelectQuery<T> select = resourceEntity.getSelect();

        List<T> objects = persister.sharedContext().select(select);

        if (resourceEntity.getMapBy() != null) {
            fetchChildren(resourceEntity, resourceEntity.getMapBy().getChildren());
        }

        fetchChildren(resourceEntity, resourceEntity.getChildren());

        return objects;
    }

    protected <T> void fetchChildren(ResourceEntity<T> parent, Map<String, ResourceEntity<?>> children) {
        for (Map.Entry<String, ResourceEntity<?>> e : children.entrySet()) {
            ResourceEntity childEntity = e.getValue();

            List childObjects = fetchEntity(childEntity);

            AgRelationship rel = parent.getAgEntity().getRelationship(e.getKey());

            BiConsumer<AgObjectId, Object> resultAccum = rel.isToMany()
                    ? (i, o) -> childEntity.addToManyResult(i, o)
                    : (i, o) -> childEntity.setToOneResult(i, o);

            assignChildrenToParent(parent, childObjects, resultAccum);
        }
    }

    /**
     * Assigns child items to the appropriate parent item
     */
    protected <T> void assignChildrenToParent(ResourceEntity<T> parentEntity, List children, BiConsumer<AgObjectId, Object> resultAccum) {
        // saves a result
        for (Object child : children) {
            if (child instanceof Object[]) {
                Object[] ids = (Object[])child;
                if (ids.length == 2) {
                    resultAccum.accept(new SimpleObjectId(ids[1]), (T) ids[0]);
                } else if (ids.length > 2) {
                    // saves entity with a compound ID
                    Map<String, Object> compoundKeys = new LinkedHashMap<>();
                    AgAttribute[] idAttributes = parentEntity.getAgEntity().getIds().toArray(new AgAttribute[0]);
                    if (idAttributes.length == (ids.length - 1)) {
                        for (int i = 1; i < ids.length; i++) {
                            compoundKeys.put(idAttributes[i - 1].getName(), ids[i]);
                        }
                    }
                    resultAccum.accept(new CompoundObjectId(compoundKeys), (T) ids[0]);
                }
            }
        }
    }
}
