package io.agrest.cayenne.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.CayenneProcessor;
import io.agrest.encoder.EntityEncoderFilter;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.map.EntityResolver;

import java.util.List;

/**
 * @since 4.8
 */
public class CayenneApplyServerParamsStage extends ApplyServerParamsStage {

    private final EntityResolver entityResolver;

    public CayenneApplyServerParamsStage(
            @Inject IConstraintsHandler constraintsHandler,
            @Inject IEncoderService encoderService,
            @Inject ICayennePersister persister,
            @Inject List<EntityEncoderFilter> filters) {
        super(constraintsHandler, encoderService, filters);
        this.entityResolver = persister.entityResolver();
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);
        tagCayenneEntities(context.getEntity());
    }

    private void tagCayenneEntities(ResourceEntity<?> entity) {
        if (entityResolver.getObjEntity(entity.getName()) != null) {
            CayenneProcessor.getOrCreateCayenneEntity(entity);
        }

        if (entity.getMapBy() != null) {
            for (NestedResourceEntity<?> child : entity.getMapBy().getChildren().values()) {
                tagCayenneEntities(child);
            }
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            tagCayenneEntities(child);
        }
    }
}
