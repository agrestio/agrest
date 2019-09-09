package io.agrest.meta.cayenne;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.property.ChildEntityListResultReader;
import io.agrest.property.ChildEntityResultReader;
import io.agrest.property.DefaultIdReader;
import io.agrest.property.IdReader;
import io.agrest.property.PropertyReader;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.agrest.meta.Types.typeForName;

/**
 * @since 3.4
 */
public class CayenneAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneAgEntityBuilder.class);

    private final EntityResolver resolver;
    private final Map<String, AgEntityOverlay> entityOverlays;

    private final Class<T> type;
    private final AgDataMap agDataMap;
    private final ObjEntity cayenneEntity;

    private final Map<String, AgAttribute> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    private boolean pojoIdReader;

    public CayenneAgEntityBuilder(
            Class<T> type,
            AgDataMap agDataMap,
            EntityResolver resolver,
            Map<String, AgEntityOverlay> entityOverlays) {

        this.resolver = resolver;
        this.entityOverlays = entityOverlays;

        this.type = type;
        this.agDataMap = agDataMap;
        this.cayenneEntity = resolver.getObjEntity(type);

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public AgEntity<T> build() {

        buildCayenneEntity();
        buildAnnotatedProperties();
        applyOverlays();

        IdReader idReader = pojoIdReader ? new DefaultIdReader(ids.keySet()) : ObjectIdReader.reader();

        return new DefaultAgEntity<>(
                cayenneEntity.getName(),
                type,
                ids,
                attributes,
                relationships,
                idReader);
    }

    private AgAttribute addId(AgAttribute id) {
        return ids.put(id.getName(), id);
    }

    private AgAttribute addAttribute(AgAttribute a) {
        return attributes.put(a.getName(), a);
    }

    private AgRelationship addRelationship(AgRelationship r) {
        return relationships.put(r.getName(), r);
    }

    protected void buildCayenneEntity() {

        for (ObjAttribute a : cayenneEntity.getAttributes()) {
            Class<?> type = typeForName(a.getType());
            addAttribute(new CayenneAgObjAttribute(a, type, DataObjectPropertyReader.reader()));
        }

        for (ObjRelationship r : cayenneEntity.getRelationships()) {

            Class<?> targetEntityType = resolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();
            AgEntity<?> targetEntity = agDataMap.getEntity(targetEntityType);

            // TODO: a decision whether to read results from the object or from the child entity (via
            //  ChildEntityResultReader and ChildEntityListResultReader) should not be dependent on the object nature,
            //  but rather on the data retrieval strategy for a given relationship

            Function<ResourceEntity<?>, PropertyReader> readerFactory = r.isToMany()
                    // "idReader" must come from parent entity.. it is not yet know here
                    ? e -> new ChildEntityListResultReader(e, e.getAgEntity().getIdReader())
                    : e -> new ChildEntityResultReader(e, e.getAgEntity().getIdReader());

            addRelationship(new CayenneAgRelationship(r, targetEntity, readerFactory));
        }

        for (DbAttribute pk : cayenneEntity.getDbEntity().getPrimaryKeys()) {
            ObjAttribute attribute = cayenneEntity.getAttributeForDbAttribute(pk);

            AgAttribute id;
            if (attribute == null) {

                // TODO: we are using a DB name for the attribute... Perhaps it should not be exposed in Ag model?
                id = new CayenneAgDbAttribute(
                        pk.getName(),
                        pk,
                        typeForName(TypesMapping.getJavaBySqlType(pk.getType())),
                        ObjectIdValueReader.reader());
            } else {
                id = new CayenneAgObjAttribute(
                        attribute,
                        typeForName(attribute.getType()),
                        DataObjectPropertyReader.reader());
            }

            addId(id);
        }
    }

    protected void buildAnnotatedProperties() {

        // load a separate entity built purely from annotations, then merge it
        // with our entity... Note that we are not cloning attributes or
        // relationship during merge... they have no references to parent and
        // can be used as is.

        AgEntity<T> annotatedEntity = new AgEntityBuilder<>(type, agDataMap).build();

        if (annotatedEntity.getIds().size() > 0) {

            // if annotated ids are present, remove all Cayenne-originated ids, regardless of their type and count
            if (!ids.isEmpty()) {
                LOGGER.debug("Cayenne ObjectId is overridden from annotations.");
                ids.clear();
            }

            // ensure ids are read from properties, not as ObjectId
            this.pojoIdReader = true;

            for (AgAttribute id : annotatedEntity.getIds()) {

                // there is a good chance a designated ID attribute is also a regular persistent attribute.. so make
                // sure we replace non-persistent with persistent...

                AgAttribute existingNonId = attributes.get(id.getName());

                // TODO: replacing with "existingNonId" duplicates the attribute (as "xyzName" and then as "id")
                AgAttribute newId = existingNonId != null ? existingNonId : id;
                addId(newId);
            }
        }

        for (AgAttribute attribute : annotatedEntity.getAttributes()) {
            AgAttribute existing = addAttribute(attribute);
            if (existing != null) {
                LOGGER.debug("Attribute '{}' is overridden from annotations.", existing.getName());
            }
        }

        for (AgRelationship relationship : annotatedEntity.getRelationships()) {

            AgRelationship existing = addRelationship(relationship);
            if (existing != null) {
                LOGGER.debug("Relationship '{}' is overridden from annotations.", existing.getName());
            }
        }
    }

    protected void applyOverlays() {

        AgEntityOverlay<?> overlay = entityOverlays.get(type.getName());
        if (overlay != null) {
            // TODO: what about overlaying ids?
            overlay.getAttributes().forEach(this::addAttribute);
            overlay.getRelationships(agDataMap).forEach(this::addRelationship);
        }
    }
}
