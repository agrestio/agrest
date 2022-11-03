package io.agrest.runtime.constraints;

import io.agrest.EntityUpdate;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * An {@link IConstraintsHandler} that ensures that no target attributes exceed
 * the defined bounds.
 *
 * @since 1.5
 */
public class ConstraintsHandler implements IConstraintsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConstraintsHandler.class);

    @Override
    public <T> void constrainUpdate(UpdateContext<T> context) {


        AgEntity<?> entity = context.getEntity().getAgEntity();

        for (AgIdPart idPart : entity.getIdParts()) {
            if (!idPart.isWritable()) {
                context.setIdUpdatesDisallowed(true);
                break;
            }
        }

        for (EntityUpdate<?> u : context.getUpdates()) {
            Iterator<String> valuesIt = u.getValues().keySet().iterator();
            while (valuesIt.hasNext()) {
                String name = valuesIt.next();

                AgAttribute a = entity.getAttribute(name);
                if (a == null) {
                    LOGGER.debug("Attribute not recognized, removing: '{}' for id {}", name, u.getId());
                    valuesIt.remove();
                    continue;
                }

                if (!a.isWritable()) {

                    // do not report default properties, as this wasn't a client's fault it got on the list, but
                    // still remove it
                    if (!context.getEntity().isDefaultAttribute(name)) {
                        LOGGER.debug("Attribute not allowed, removing: '{}' for id {}", name, u.getId());
                    }

                    valuesIt.remove();
                }
            }

            // updates are not hierarchical yet, so only process one level of relationships. No need for recursive checks
            Iterator<String> relatedIdsIt = u.getRelatedIds().keySet().iterator();
            while (relatedIdsIt.hasNext()) {
                String name = relatedIdsIt.next();

                AgRelationship r = entity.getRelationship(name);

                if (r == null) {
                    LOGGER.debug("Relationship not recognized, removing: '{}' for id {}", name, u.getId());
                    relatedIdsIt.remove();
                    continue;
                }

                if (!r.isWritable()) {
                    LOGGER.debug("Relationship not allowed, removing: '{}' for id {}", name, u.getId());
                    relatedIdsIt.remove();
                }
            }
        }
    }

    @Override
    public <T> void constrainResponse(ResourceEntity<T> entity) {
        constrainForRead(entity);
    }

    @Override
    public <T> void constrainResponseSize(ResourceEntity<T> entity, SizeConstraints sizeConstraints) {
        if (sizeConstraints != null) {
            applySizeConstraintsForRead(entity, sizeConstraints);
        }
    }

    protected void applySizeConstraintsForRead(ResourceEntity<?> entity, SizeConstraints constraints) {

        // fetchOffset - do not exceed source offset
        int upperOffset = constraints.getFetchOffset();
        if (upperOffset > 0 && (entity.getStart() < 0 || entity.getStart() > upperOffset)) {
            LOGGER.info("Reducing fetch offset from " + entity.getStart() + " to max allowed value of "
                    + upperOffset);
            entity.setStart(upperOffset);
        }

        // fetchLimit - do not exceed source limit
        int upperLimit = constraints.getFetchLimit();
        if (upperLimit > 0 && (entity.getLimit() <= 0 || entity.getLimit() > upperLimit)) {
            LOGGER.info(
                    "Reducing fetch limit from " + entity.getLimit() + " to max allowed value of " + upperLimit);
            entity.setLimit(upperLimit);
        }
    }


    <T> void constrainForRead(ResourceEntity<T> entity) {

        if (entity.isIdIncluded()) {

            // there's no ID overlays yet, so check AgEntity Id attributes for readability
            for (AgIdPart idPart : entity.getAgEntity().getIdParts()) {
                if (!idPart.isReadable()) {
                    entity.excludeId();
                    break;
                }
            }
        }

        // check attributes from ResourceEntity, that are already overlaid with request-specific rules
        Iterator<Map.Entry<String, AgAttribute>> ait = entity.getAttributes().entrySet().iterator();
        while (ait.hasNext()) {

            Map.Entry<String, AgAttribute> a = ait.next();
            if (!a.getValue().isReadable()) {

                // hack: do not report default properties, as this wasn't a client's fault it go there..
                if (!entity.isDefaultAttribute(a.getKey())) {
                    LOGGER.info("Attribute not allowed, removing: {}", a);
                }

                ait.remove();
            }
        }

        Iterator<Map.Entry<String, RelatedResourceEntity<?>>> rit = entity.getChildren().entrySet().iterator();
        while (rit.hasNext()) {

            Map.Entry<String, RelatedResourceEntity<?>> r = rit.next();
            if (r.getValue().getIncoming().isReadable()) {
                constrainForRead(r.getValue());
            } else {
                LOGGER.info("Relationship not allowed, removing: {}", r.getKey());
                rit.remove();
            }
        }
    }
}
