package io.agrest.runtime.constraints;

import io.agrest.AgException;
import io.agrest.EntityUpdate;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

class WritePropertyFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(WritePropertyFilter.class);

    public static <T> void apply(ResourceEntity<T> entity, Collection<EntityUpdate<T>> updates) {

        AgEntity<T> agEntity = entity.getAgEntity();

        boolean disallowIdUpdates = false;
        for (AgIdPart idPart : agEntity.getIdParts()) {
            if (!idPart.isWritable()) {
                disallowIdUpdates = true;
                break;
            }
        }

        for (EntityUpdate<?> u : updates) {

            // unlike attributes and relationships, when ID is not writable, it feels like we can't just quietly
            // exclude it from update, we must throw. Especially true for IDs coming from URLs (vs those coming
            // from JSON), but let's handle both consistently

            if (disallowIdUpdates) {
                Map<String, Object> id = u.getIdParts();
                if (!id.isEmpty()) {
                    throw AgException.badRequest("Setting ID explicitly is not allowed: %s", id);
                }
            }

            Iterator<String> attributesIt = u.getAttributes().keySet().iterator();
            while (attributesIt.hasNext()) {
                String name = attributesIt.next();

                AgAttribute a = agEntity.getAttribute(name);
                if (a == null) {
                    LOGGER.debug("Attribute not recognized, removing: '{}' for id {}", name, u.getIdParts());
                    attributesIt.remove();
                    continue;
                }

                if (!a.isWritable()) {

                    // do not report default properties, as this wasn't a client's fault it got on the list, but
                    // still remove it
                    if (!entity.getBaseProjection().isDefaultAttribute(name)) {
                        LOGGER.debug("Attribute not allowed, removing: '{}' for id {}", name, u.getIdParts());
                    }

                    attributesIt.remove();
                }
            }

            // TODO: recursive checks on relationships

            Consumer<Set<String>> checkIds = s -> {
                Iterator<String> relationshipsIt = s.iterator();
                while (relationshipsIt.hasNext()) {
                    String name = relationshipsIt.next();

                    AgRelationship r = agEntity.getRelationship(name);

                    if (r == null) {
                        LOGGER.debug("Relationship not recognized, removing: '{}' for id {}", name, u.getIdParts());
                        relationshipsIt.remove();
                        continue;
                    }

                    if (!r.isWritable()) {
                        LOGGER.debug("Relationship not allowed, removing: '{}' for id {}", name, u.getIdParts());
                        relationshipsIt.remove();
                    }
                }
            };

            checkIds.accept(u.getToOneIds().keySet());
            checkIds.accept(u.getToManyIds().keySet());
        }
    }
}
