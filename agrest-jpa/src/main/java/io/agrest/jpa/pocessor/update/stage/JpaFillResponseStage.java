package io.agrest.jpa.pocessor.update.stage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.agrest.AgException;
import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.SimpleObjectId;
import io.agrest.ToManyResourceEntity;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.property.PropertyReader;
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

        PropertyReader idReader = entity.getAgEntity().getIdReader();
        Map<String, NestedResourceEntity<?>> children = entity.getChildren();
        EntityType<?> entityType = metamodel.entity(entity.getType());

        if (!children.isEmpty()) {

            for (Map.Entry<String, NestedResourceEntity<?>> e : children.entrySet()) {
                NestedResourceEntity childEntity = e.getValue();

                Object result = JpaUtil.readProperty(root, entityType.getAttribute(e.getKey()));
                if (result == null) { // TODO: could it be some sort of a fault?
                    continue;
                }

                Map<String, Object> idMap = (Map<String, Object>) idReader.value(root);
                AgObjectId id = idMap.size() > 1
                        ? new CompoundObjectId(idMap)
                        : new SimpleObjectId(idMap.values().iterator().next());

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
