package io.agrest.runtime.processor.meta;

import io.agrest.NestedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgResource;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.constraints.IConstraintsHandler;
import io.agrest.runtime.encoder.IEncoderService;
import io.agrest.runtime.meta.IResourceMetadataService;
import org.apache.cayenne.di.Inject;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 2.7
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class CollectMetadataStage implements Processor<MetadataContext<?>> {

    private AgDataMap dataMap;
    private IResourceMetadataService resourceMetadataService;
    private IEncoderService encoderService;
    private IConstraintsHandler constraintsHandler;

    public CollectMetadataStage(
            @Inject AgDataMap dataMap,
            @Inject IResourceMetadataService resourceMetadataService,
            @Inject IEncoderService encoderService,
            @Inject IConstraintsHandler constraintsHandler) {

        this.dataMap = dataMap;
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
        AgEntity<T> entity = dataMap.getEntity(context.getType());
        Collection<AgResource<?>> resources = resourceMetadataService.getAgResources(context.getResource());
        Collection<AgResource<T>> filteredResources = new ArrayList<>(resources.size());

        for (AgResource<?> resource : resources) {
            AgEntity<?> resourceEntity = resource.getEntity();
            if (resourceEntity != null && resourceEntity.getName().equals(entity.getName())) {
                filteredResources.add((AgResource<T>) resource);
            }
        }

        RootResourceEntity<T> resourceEntity = createDefaultResourceEntity(entity);
        constraintsHandler.constrainResponse(resourceEntity, null);
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

    private <T> RootResourceEntity<T> createDefaultResourceEntity(AgEntity<T> entity) {

        // TODO: support entity overlays in meta requests
        RootResourceEntity<T> resourceEntity = new RootResourceEntity<>(entity);

        for (AgAttribute a : entity.getAttributes()) {
            resourceEntity.addAttribute(a, false);
        }

        for (AgRelationship r : entity.getRelationships()) {
            // TODO: support entity overlays in meta requests
            NestedResourceEntity<?> child =  r.isToMany()
                    ? new ToManyResourceEntity<>(resourceEntity.getAgEntity(), resourceEntity, r)
                    : new ToOneResourceEntity<>(resourceEntity.getAgEntity(), resourceEntity, r);

            resourceEntity.getChildren().put(r.getName(), child);
        }

        return resourceEntity;
    }
}
