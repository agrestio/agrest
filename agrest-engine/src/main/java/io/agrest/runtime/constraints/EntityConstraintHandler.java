package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;
import io.agrest.EntityUpdate;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.runtime.processor.update.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @since 1.6
 */
class EntityConstraintHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityConstraintHandler.class);

    private EntityConstraintSource forRead;
    private EntityConstraintSource forWrite;

    EntityConstraintHandler(List<EntityConstraint> defaultReadConstraints,
                            List<EntityConstraint> defaultWriteConstraints) {

        // note that explicit defaults override annotations
        // annotation-based constraints will be compiled dynamically
        ConcurrentMap<String, EntityConstraint> readMap = new ConcurrentHashMap<>();
        for (EntityConstraint c : defaultReadConstraints) {
            readMap.put(c.getEntityName(), c);
        }

        ConcurrentMap<String, EntityConstraint> writeMap = new ConcurrentHashMap<>();
        for (EntityConstraint c : defaultWriteConstraints) {
            writeMap.put(c.getEntityName(), c);
        }

        this.forRead = new EntityConstraintSource(readMap) {

            @Override
            protected AccessibleProperties findAccessible(AgEntity<?> entity) {

                AccessibleProperties ap = new AccessibleProperties();

                for(AgIdPart id : entity.getIdParts()) {
                    if(id.isReadable()) {
                        ap.idParts.add(id.getName());
                    }
                }

                for(AgAttribute a : entity.getAttributes()) {
                    if(a.isReadable()) {
                        ap.attributes.add(a.getName());
                    }
                }

                for(AgRelationship r : entity.getRelationships()) {
                    if(r.isReadable()) {
                        ap.relationships.add(r.getName());
                    }
                }

                return ap;
            }
        };

        this.forWrite = new EntityConstraintSource(writeMap) {
            @Override
            protected AccessibleProperties findAccessible(AgEntity<?> entity) {
                AccessibleProperties ap = new AccessibleProperties();

                for(AgIdPart id : entity.getIdParts()) {
                    if(id.isWritable()) {
                        ap.idParts.add(id.getName());
                    }
                }

                for(AgAttribute a : entity.getAttributes()) {
                    if(a.isWritable()) {
                        ap.attributes.add(a.getName());
                    }
                }

                for(AgRelationship r : entity.getRelationships()) {
                    if(r.isWritable()) {
                        ap.relationships.add(r.getName());
                    }
                }

                return ap;
            }
        };
    }

    void constrainResponse(ResourceEntity<?> resourceEntity) {
        constrainForRead(resourceEntity);
    }

    void constrainUpdate(UpdateContext<?> context) {

        EntityConstraint c = forWrite.getOrCreate(context.getEntity().getAgEntity());

        if (!c.allowsId()) {
            context.setIdUpdatesDisallowed(true);
        }

        // updates are not hierarchical yet, so simply check attributes...
        // TODO: updates may contain FKs ... need to handle that

        if (!c.allowsAllAttributes()) {
            for (EntityUpdate<?> u : context.getUpdates()) {

                // exclude disallowed attributes
                Iterator<String> it = u.getValues().keySet().iterator();
                while (it.hasNext()) {
                    String attr = it.next();
                    if (!c.allowsAttribute(attr)) {

                        // do not report default properties, as this wasn't a client's fault it go there..
                        if (!context.getEntity().isDefaultAttribute(attr)) {
                            LOGGER.info("Attribute not allowed, removing: {} for id {}", attr, u.getId());
                        }

                        it.remove();
                    }
                }
            }
        }

        for (EntityUpdate<?> u : context.getUpdates()) {
            Iterator<String> it = u.getRelatedIds().keySet().iterator();
            while (it.hasNext()) {
                String relationship = it.next();
                if (!c.allowsRelationship(relationship)) {
                    LOGGER.info("Relationship not allowed, removing: {} for id {}", relationship, u.getId());
                    it.remove();
                }
            }
        }
    }

    void constrainForRead(ResourceEntity<?> entity) {

        EntityConstraint c = forRead.getOrCreate(entity.getAgEntity());

        if (!c.allowsId()) {
            entity.excludeId();
        }

        if (!c.allowsAllAttributes()) {
            Iterator<AgAttribute> ait = entity.getAttributes().values().iterator();
            while (ait.hasNext()) {

                String a = ait.next().getName();
                if (!c.allowsAttribute(a)) {

                    // hack: do not report default properties, as this wasn't a
                    // client's fault it go there..
                    if (!entity.isDefaultAttribute(a)) {
                        LOGGER.info("Attribute not allowed, removing: {}", a);
                    }

                    ait.remove();
                }
            }
        }

        Iterator<Entry<String, NestedResourceEntity<?>>> rit = entity.getChildren().entrySet().iterator();
        while (rit.hasNext()) {

            Entry<String, NestedResourceEntity<?>> e = rit.next();

            if (c.allowsRelationship(e.getKey())) {
                constrainForRead(e.getValue());
            } else {

                // do not report default properties, as this wasn't a client's
                // fault it go there..
                if (!entity.isDefaultAttribute(e.getKey())) {
                    LOGGER.info("Relationship not allowed, removing: {}", e.getKey());
                }

                rit.remove();
            }
        }
    }
}
