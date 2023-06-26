package io.agrest.runtime.constraints;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.ResourceEntityProjection;
import io.agrest.SizeConstraints;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        boolean disallowIdUpdates = false;
        for (AgIdPart idPart : entity.getIdParts()) {
            if (!idPart.isWritable()) {
                disallowIdUpdates = true;
                break;
            }
        }

        for (EntityUpdate<?> u : context.getUpdates()) {

            // unlike attributes and relationships, when ID is not writable, it feels like we can't just quietly
            // exclude it from update, we must throw. Especially true for IDs coming from URLs (vs those coming
            // from JSON), but let's handle both consistently

            if (disallowIdUpdates) {
                Map<String, Object> id = u.getId();
                if (id != null && !id.isEmpty()) {
                    throw AgException.badRequest("Setting ID explicitly is not allowed: %s", id);
                }
            }

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
                    if (!context.getEntity().getBaseProjection().isDefaultAttribute(name)) {
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
        for (ResourceEntityProjection<?> projection : entity.getProjections()) {

            List<String> aToRemove = null;

            for (AgAttribute a : projection.getAttributes()) {
                if (!a.isReadable()) {

                    // do not report default properties, as this wasn't a client's fault it go there..
                    if (!projection.isDefaultAttribute(a.getName())) {
                        LOGGER.info("Attribute not allowed, removing: {}", a);
                    }

                    // can't remove in the iterator, so collect removed properties
                    if (aToRemove == null) {
                        aToRemove = new ArrayList<>();
                    }
                    aToRemove.add(a.getName());
                }
            }

            if (aToRemove != null) {
                aToRemove.forEach(projection::removeAttribute);
            }

            List<String> rToRemove = null;

            for (AgRelationship r : projection.getRelationships()) {
                if (r.isReadable()) {
                    constrainForRead(entity.getChild(r.getName()));
                } else {
                    LOGGER.info("Relationship not allowed, removing: {}", r.getName());
                    // can't remove in the iterator, so collect removed properties
                    if (rToRemove == null) {
                        rToRemove = new ArrayList<>();
                    }
                    rToRemove.add(r.getName());
                }
            }

            if (rToRemove != null) {
                rToRemove.forEach(projection::removeRelationship);
            }
        }
    }
}
