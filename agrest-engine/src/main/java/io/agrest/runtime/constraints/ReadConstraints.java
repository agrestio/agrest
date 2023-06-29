package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.ResourceEntityProjection;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

class ReadConstraints {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadConstraints.class);

    static <T> void apply(ResourceEntity<T> entity) {

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
                    apply(entity.getChild(r.getName()));
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
