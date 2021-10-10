package io.agrest.runtime.constraints;

import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgIdPart;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @since 1.6
 */
class EntityConstraintHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConstraintHandler.class);

    void constrainResponse(ResourceEntity<?> resourceEntity) {
        constrainForRead(resourceEntity);
    }

    void constrainUpdate(UpdateContext<?> context) {

        RootResourceEntity<?> entity = context.getEntity();

        if (entity.isIdIncluded()) {

            // there's no ID overlays yet, so check AgEntity id attributes for writeability
            for (AgIdPart idPart : entity.getAgEntity().getIdParts()) {
                if (!idPart.isWritable()) {
                    context.setIdUpdatesDisallowed(true);
                    break;
                }
            }
        }

        for (EntityUpdate<?> u : context.getUpdates()) {
            Iterator<String> it = u.getValues().keySet().iterator();
            while (it.hasNext()) {
                String name = it.next();

                AgAttribute a = entity.getAttribute(name);
                if (a != null) {

                    if (!a.isWritable()) {

                        // do not report default properties, as this wasn't a client's fault it go there..
                        if (!context.getEntity().isDefaultAttribute(name)) {
                            LOGGER.info("Attribute not allowed, removing: {} for id {}", name, u.getId());
                        }

                        it.remove();

                    }

                    continue;
                }

                // updates are not hierarchical yet, so no need to recursively process the "child"
                NestedResourceEntity<?> child = entity.getChild(name);
                if (child != null) {
                    if (child.getIncoming().isWritable()) {
                        LOGGER.info("Relationship not allowed, removing: {} for id {}", name, u.getId());
                        it.remove();
                    }

                    continue;
                }

                // not an entity property, remove
                it.remove();
            }
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
        Iterator<Entry<String, AgAttribute>> ait = entity.getAttributes().entrySet().iterator();
        while (ait.hasNext()) {

            Entry<String, AgAttribute> a = ait.next();
            if (!a.getValue().isReadable()) {

                // hack: do not report default properties, as this wasn't a client's fault it go there..
                if (!entity.isDefaultAttribute(a.getKey())) {
                    LOGGER.info("Attribute not allowed, removing: {}", a);
                }

                ait.remove();
            }
        }

        Iterator<Entry<String, NestedResourceEntity<?>>> rit = entity.getChildren().entrySet().iterator();
        while (rit.hasNext()) {

            Entry<String, NestedResourceEntity<?>> r = rit.next();
            if (r.getValue().getIncoming().isReadable()) {
                constrainForRead(r.getValue());
            } else {
                LOGGER.info("Relationship not allowed, removing: {}", r.getKey());
                rit.remove();
            }
        }
    }
}
