package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.id.AgObjectId;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.reader.DataReader;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.processor.update.stage.UpdateFillResponseStage;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public abstract class JpaFillResponseStage extends UpdateFillResponseStage {

    private final Metamodel metamodel;

    public JpaFillResponseStage(@Inject IAgJpaPersister persister) {
        this.metamodel = persister.metamodel();
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute((UpdateContext<Object>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected abstract int getHttpStatus(UpdateContext<Object> context);

    @SuppressWarnings("unchecked")
    protected void doExecute(UpdateContext<Object> context) {

        context.setStatus(getHttpStatus(context));

        if (context.isIncludingDataInResponse()) {

            // Updated objects are attached to EntityUpdate instances ... Create a list of unique updated
            // objects in the order corresponding to their initial appearance in the updates collection.

            // if there are dupes, the list size will be smaller... sizing it pessimistically
            List<Object> objects = new ArrayList<>(context.getUpdates().size());

            // 'seen' is for a case of multiple updates per object in a request
            Set<Object> seen = new HashSet<>();

            for (EntityUpdate<Object> u : context.getUpdates()) {

                Object o = u.getMergedTo();
                if (o != null && seen.add(o)) {
                    objects.add(o);

                    // TODO: child entities should be seeded via a special NestedDataResolver to read from parent
                    //  instead of manually traversing objects
                    assignChildrenToParent(o, context.getEntity());
                }
            }

            context.getEntity().setData(objects);
        }
    }

    protected void assignChildrenToParent(Object root, ResourceEntity<?> entity) {

        DataReader idReader = entity.getAgEntity().getIdReader();
        Map<String, RelatedResourceEntity<?>> children = entity.getChildren();
        EntityType<?> entityType = metamodel.entity(entity.getType());

        if (!children.isEmpty()) {

            for (Map.Entry<String, RelatedResourceEntity<?>> e : children.entrySet()) {
                RelatedResourceEntity childEntity = e.getValue();

                Object result = JpaUtil.readProperty(root, entityType.getAttribute(e.getKey()));
                if (result == null) { // TODO: could it be some sort of a fault?
                    continue;
                }

                Map<String, Object> idMap = (Map<String, Object>) idReader.read(root);
                AgObjectId id = idMap.size() > 1
                        ? AgObjectId.ofMap(idMap)
                        : AgObjectId.of(idMap.values().iterator().next());

                if (childEntity instanceof ToManyResourceEntity) {
                    List r = (List) result;
                    ((ToManyResourceEntity) childEntity).setData(id, r);
                    for (Object ro : r) {
                        assignChildrenToParent(ro, childEntity);
                    }

                } else {
                    childEntity.addData(id, result);
                    assignChildrenToParent(result, childEntity);
                }
            }
        }
    }
}
