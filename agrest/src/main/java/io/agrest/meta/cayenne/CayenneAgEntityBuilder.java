package io.agrest.meta.cayenne;

import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityBuilder;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgRelationshipOverlay;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.DefaultAgRelationship;
import io.agrest.property.DefaultIdReader;
import io.agrest.property.IdReader;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.meta.Types.typeForName;

/**
 * @since 3.4
 */
public class CayenneAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneAgEntityBuilder.class);

    private final EntityResolver cayenneResolver;
    private final Class<T> type;
    private final AgDataMap agDataMap;
    private final ObjEntity cayenneEntity;
    private final Map<String, AgAttribute> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    private AgEntityOverlay<T> overlay;
    private boolean pojoIdReader;
    private RootDataResolver<T> rootDataResolver;
    private NestedDataResolver<T> nestedDataResolver;
    private NestedDataResolver pojoNestedResolver;

    public CayenneAgEntityBuilder(Class<T> type, AgDataMap agDataMap, EntityResolver cayenneResolver) {

        this.cayenneResolver = cayenneResolver;

        this.type = type;
        this.agDataMap = agDataMap;
        this.cayenneEntity = cayenneResolver.getObjEntity(type);

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public CayenneAgEntityBuilder<T> overlay(AgEntityOverlay<T> overlay) {
        this.overlay = overlay;
        return this;
    }

    public CayenneAgEntityBuilder<T> rootDataResolver(RootDataResolver<T> resolver) {
        this.rootDataResolver = resolver;
        return this;
    }

    public CayenneAgEntityBuilder<T> nestedDataResolver(NestedDataResolver<T> resolver) {
        this.nestedDataResolver = resolver;
        return this;
    }

    public CayenneAgEntityBuilder<T> pojoNestedDataResolver(NestedDataResolver<T> resolver) {
        this.pojoNestedResolver = resolver;
        return this;
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
                idReader,
                rootDataResolver != null ? rootDataResolver : ThrowingRootDataResolver.getInstance());
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
            addAttribute(new DefaultAgAttribute(a.getName(), type, new ASTObjPath(a.getName()), DataObjectPropertyReader.reader()));
        }

        for (ObjRelationship r : cayenneEntity.getRelationships()) {

            Class<?> targetEntityType = cayenneResolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();

            addRelationship(new DefaultAgRelationship(
                    r.getName(),
                    // 'agDataMap.getEntity' will compile the entity on the fly if needed
                    agDataMap.getEntity(targetEntityType),
                    r.isToMany(),
                    nestedDataResolver));
        }

        for (DbAttribute pk : cayenneEntity.getDbEntity().getPrimaryKeys()) {
            ObjAttribute attribute = cayenneEntity.getAttributeForDbAttribute(pk);

            AgAttribute id;
            if (attribute == null) {

                // TODO: we are using a DB name for the attribute... Perhaps it should not be exposed in Ag model?
                id = new DefaultAgAttribute(
                        pk.getName(),
                        typeForName(TypesMapping.getJavaBySqlType(pk.getType())),
                        new ASTDbPath(pk.getName()),
                        ObjectIdValueReader.reader());
            } else {
                id = new DefaultAgAttribute(
                        attribute.getName(),
                        typeForName(attribute.getType()),
                        new ASTObjPath(attribute.getName()),
                        DataObjectPropertyReader.reader());
            }

            addId(id);
        }
    }

    protected void buildAnnotatedProperties() {

        // Load a separate entity built purely from annotations, then merge it with our entity. We are not cloning
        // attributes or relationship during merge... they have no references to parent and can be used as is.

        AgEntity<T> annotatedEntity = new AgEntityBuilder<>(type, agDataMap)
                .nestedDataResolver(pojoNestedResolver)
                .build();

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
        if (overlay != null) {
            // TODO: what about overlaying ids?
            overlay.getAttributes().forEach(this::addAttribute);

            overlay.getRelationshipOverlays().forEach(this::loadRelationshipOverlay);
        }
    }

    protected void loadRelationshipOverlay(AgRelationshipOverlay overlay) {
        AgRelationship relationship = overlay.resolve(relationships.get(overlay.getName()), agDataMap);
        if (relationship != null) {
            addRelationship(relationship);
        }
    }
}
