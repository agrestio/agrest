package io.agrest.runtime.processor.meta;

import io.agrest.ResourceEntity;
import io.agrest.constraints.Constraint;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgResource;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.IResourceMetadataService;
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
    protected <T, E> void doExecute(MetadataContext<T> context) {
        AgEntity<T> entity = metadataService.getAgEntity(context.getType());
        Collection<AgResource<?>> resources = resourceMetadataService.getAgResources(context.getResource());
        Collection<AgResource<T>> filteredResources = new ArrayList<>(resources.size());

        for (AgResource<?> resource : resources) {
            AgEntity<?> resourceEntity = resource.getEntity();
            if (resourceEntity != null && resourceEntity.getName().equals(entity.getName())) {
                filteredResources.add((AgResource<T>) resource);
            }
        }

        ResourceEntity<T, E> resourceEntity = createDefaultResourceEntity(entity);
        Constraint<T, E> constraint = (Constraint<T, E>)context.getConstraint();
        constraintsHandler.constrainResponse(resourceEntity, null, constraint);
        resourceEntity.setApplicationBase(getBaseUrl(context));

        context.setResources(filteredResources);
        context.setEncoder(encoderService.metadataEncoder(resourceEntity));
    }

    private <T> String getBaseUrl(MetadataContext<T> context) {
        return resourceMetadataService.getBaseUrl().orElseGet(() ->
                context.getUriInfo() != null
                        ? context.getUriInfo().getBaseUri().toString() : null
        );
    }

    private <T, E> ResourceEntity<T, E> createDefaultResourceEntity(AgEntity<T> entity) {
        ResourceEntity<T, E> resourceEntity = new ResourceEntity<>(entity);

        for (AgAttribute a : entity.getAttributes()) {
            resourceEntity.getAttributes().put(a.getName(), a);
        }

        for (AgRelationship r : entity.getRelationships()) {
            ResourceEntity<?, E> child = new ResourceEntity<>(r.getTargetEntity(), r);
            resourceEntity.getChildren().put(r.getName(), child);
        }

        return resourceEntity;
    }
}
