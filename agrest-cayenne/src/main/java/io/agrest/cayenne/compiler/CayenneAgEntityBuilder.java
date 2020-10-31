package io.agrest.cayenne.compiler;

import io.agrest.compiler.AnnotationsAgEntityBuilder;
import io.agrest.meta.*;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.map.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.base.reflect.Types.typeForName;

/**
 * @since 3.4
 */
public class CayenneAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneAgEntityBuilder.class);

    private final EntityResolver cayenneResolver;
    private final Class<T> type;
    private final AgDataMap agDataMap;
    private final ObjEntity cayenneEntity;
    private final Map<String, AgIdPart> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    private AgEntityOverlay<T> overlay;
    private RootDataResolver<T> rootDataResolver;
    private NestedDataResolver<T> nestedDataResolver;

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

    public AgEntity<T> build() {

        buildCayenneEntity();
        buildAnnotatedProperties();
        applyOverlays();

        return new DefaultAgEntity<>(
                cayenneEntity.getName(),
                type,
                ids,
                attributes,
                relationships,
                rootDataResolver != null ? rootDataResolver : ThrowingRootDataResolver.getInstance());
    }

    private void addId(AgIdPart id) {
        ids.put(id.getName(), id);
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
            String name = a.getName();
            addAttribute(new DefaultAgAttribute(name, type, DataObjectPropertyReader.reader(name)));
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

            AgIdPart id;
            if (attribute == null) {

                // TODO: we are using a DB name for the attribute... Perhaps it should not be exposed in Ag model?
                id = new DefaultAgIdPart(
                        pk.getName(),
                        typeForName(TypesMapping.getJavaBySqlType(pk.getType())),
                        ObjectIdValueReader.reader(pk.getName()),
                        new ASTDbPath(pk.getName())
                );
            } else {
                id = new DefaultAgIdPart(
                        attribute.getName(),
                        typeForName(attribute.getType()),
                        DataObjectPropertyReader.reader(attribute.getName()),
                        new ASTObjPath(attribute.getName()));
            }

            addId(id);
        }
    }

    protected void buildAnnotatedProperties() {

        // Load a separate entity built purely from annotations, then merge it with our entity. We are not cloning
        // attributes or relationship during merge... they have no references to parent and can be used as is.

        AgEntity<T> annotatedEntity = new AnnotationsAgEntityBuilder<>(type, agDataMap).build();

        if (annotatedEntity.getIdParts().size() > 0) {

            // if annotated ids are present, remove all Cayenne-originated ids, regardless of their type and count
            if (!ids.isEmpty()) {
                LOGGER.debug("Cayenne ObjectId is overridden from annotations.");
                ids.clear();
            }

            // TODO: we should remove a possible matching regular persistent attributes from the model, since it was
            //  also declared as ID, and hence should not be exposed as an attribute anymore
            annotatedEntity.getIdParts().forEach(this::addId);
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
            overlay.getExcludes().forEach(this::removeIdOrAttributeOrRelationship);

            if (overlay.getRootDataResolver() != null) {
                this.rootDataResolver = overlay.getRootDataResolver();
            }
        }
    }

    protected void loadRelationshipOverlay(AgRelationshipOverlay overlay) {
        AgRelationship relationship = overlay.resolve(relationships.get(overlay.getName()), agDataMap);
        if (relationship != null) {
            addRelationship(relationship);
        }
    }

    protected void removeIdOrAttributeOrRelationship(String name) {
        ids.remove(name);
        attributes.remove(name);
        relationships.remove(name);
    }
}
