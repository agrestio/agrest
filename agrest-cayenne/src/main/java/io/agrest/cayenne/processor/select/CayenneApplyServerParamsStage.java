package io.agrest.cayenne.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;

/**
 * @since 4.8
 */
public class CayenneApplyServerParamsStage extends ApplyServerParamsStage {

    private final EntityResolver entityResolver;

    public CayenneApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject ICayennePersister persister) {
        super(constraintsHandler);
        this.entityResolver = persister.entityResolver();
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);
        tagRootEntity(context.getEntity());
    }

    private void tagRootEntity(RootResourceEntity<?> entity) {
        if (entityResolver.getObjEntity(entity.getName()) != null) {
            CayenneProcessor.getOrCreateRootEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (NestedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }

    private void tagNestedEntity(NestedResourceEntity<?> entity) {
        if (entityResolver.getObjEntity(entity.getName()) != null) {
            CayenneProcessor.getOrCreateNestedEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (NestedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagNestedEntity(child);
            }
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            tagNestedEntity(child);
        }
    }
}
