package io.agrest.cayenne.processor.select.stage;

import io.agrest.RelatedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;

/**
 * @since 4.8
 */
public class CayenneSelectApplyServerParamsStage extends SelectApplyServerParamsStage {

    private final EntityResolver entityResolver;

    public CayenneSelectApplyServerParamsStage(
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
            for (RelatedResourceEntity<?> child : entity.getMapBy().getChildren()) {
                tagRelatedEntity(child);
            }
        }

        for (RelatedResourceEntity<?> child : entity.getChildren()) {
            tagRelatedEntity(child);
        }
    }

    private void tagRelatedEntity(RelatedResourceEntity<?> entity) {
        if (entityResolver.getObjEntity(entity.getName()) != null) {
            CayenneProcessor.getOrCreateRelatedEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (RelatedResourceEntity<?> child : entity.getMapBy().getChildren()) {
                tagRelatedEntity(child);
            }
        }

        for (RelatedResourceEntity<?> child : entity.getChildren()) {
            tagRelatedEntity(child);
        }
    }
}
