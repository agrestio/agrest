package com.nhl.link.rest.runtime.processor.meta;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.constraints.IConstraintsHandler;
import com.nhl.link.rest.runtime.encoder.IEncoderService;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.meta.IResourceMetadataService;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 */
public class CollectMetadataStage implements Processor<MetadataContext<?>> {

    private IMetadataService metadataService;
    private IResourceMetadataService resourceMetadataService;
    private IEncoderService encoderService;
    private IConstraintsHandler constraintsHandler;

    public CollectMetadataStage(
            @Inject IMetadataService metadataService,
            @Inject IResourceMetadataService resourceMetadataService,
            @Inject IEncoderService encoderService,
            @Inject IConstraintsHandler constraintsHandler) {

        this.metadataService = metadataService;
        this.resourceMetadataService = resourceMetadataService;
        this.encoderService = encoderService;
        this.constraintsHandler = constraintsHandler;
    }

    @Override
    public ProcessorOutcome execute(MetadataContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    @SuppressWarnings("unchecked")
    protected <T> void doExecute(MetadataContext<T> context) {
        LrEntity<T> entity = metadataService.getLrEntity(context.getType());
        Collection<LrResource<?>> resources = resourceMetadataService.getLrResources(context.getResource());
        Collection<LrResource<T>> filteredResources = new ArrayList<>(resources.size());

        for (LrResource<?> resource : resources) {
            LrEntity<?> resourceEntity = resource.getEntity();
            if (resourceEntity != null && resourceEntity.getName().equals(entity.getName())) {
                filteredResources.add((LrResource<T>) resource);
            }
        }

        ResourceEntity<T> resourceEntity = createDefaultResourceEntity(entity);
        constraintsHandler.constrainResponse(resourceEntity, null, context.getConstraint());
        resourceEntity.setApplicationBase(context.getApplicationBase());

        context.setResources(filteredResources);
        context.setEncoder(encoderService.metadataEncoder(resourceEntity));
    }

    private <T> ResourceEntity<T> createDefaultResourceEntity(LrEntity<T> entity) {
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        for (LrAttribute a : entity.getAttributes()) {
            resourceEntity.getAttributes().put(a.getName(), a);
        }

        for (LrRelationship r : entity.getRelationships()) {
            ResourceEntity<?> child = new ResourceEntity<>(r.getTargetEntity(), r);
            resourceEntity.getChildren().put(r.getName(), child);
        }

        return resourceEntity;
    }
}
