package io.agrest.cayenne.compiler;

import io.agrest.PathConstants;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.compiler.AnnotationsAgEntityBuilder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.DefaultAgIdPart;
import io.agrest.meta.DefaultAgRelationship;
import io.agrest.resolver.NestedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.agrest.reflect.Types.typeForName;

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
        return applyOverlay(buildEntity());
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

            // check for naming conflicts with Agrest "id"
            if (PathConstants.ID_PK_ATTRIBUTE.equals(a.getName())) {

                // TODO: We allow ObjAttributes that are PKs to be used as regular Agrest attributes, except
                //   when they are called "id". Such a distinction may be confusing
                if (a.isPrimaryKey()) {
                    // this attribute will be added below as the ID (or a part of the ID)
                    continue;
                }
                
                LOGGER.warn(
                        "Non-PK attribute in {} is called '{}', which conflicts with the Agrest default ID property",
                        cayenneEntity.getName(),
                        a.getName());
            }

            Class<?> type = typeForName(a.getType());
            String name = a.getName();
            // by default adding attributes as readable and writable... @AgAttribute annotation on a getter may override this
            addAttribute(new DefaultAgAttribute(name, type, true, true, DataObjectPropertyReader.reader(name)));
        }

        for (ObjRelationship r : cayenneEntity.getRelationships()) {

            // check for naming conflicts with Agrest "id"
            if (PathConstants.ID_PK_ATTRIBUTE.equals(r.getName())) {
                LOGGER.warn(
                        "A relationship in {} is called '{}', which conflicts with the Agrest default ID property",
                        cayenneEntity.getName(),
                        r.getName());
            }

            Class<?> targetEntityType = cayenneResolver.getClassDescriptor(r.getTargetEntityName()).getObjectClass();

            addRelationship(new DefaultAgRelationship(
                    r.getName(),
                    // 'agDataMap.getEntity' will compile the entity on the fly if needed
                    agDataMap.getEntity(targetEntityType),
                    r.isToMany(),

                    // TODO: maybe this should be "false, false" by default, giving us a default request model
                    //  (i.e. all attributes, no relationships)
                    true,
                    true,
                    nestedDataResolver));
        }

        for (DbAttribute pk : cayenneEntity.getDbEntity().getPrimaryKeys()) {
            ObjAttribute attribute = cayenneEntity.getAttributeForDbAttribute(pk);

            AgIdPart id;
            if (attribute == null) {

                id = new DefaultAgIdPart(
                        // TODO: we are exposing DB column name here
                        ASTDbPath.DB_PREFIX + pk.getName(),
                        typeForName(TypesMapping.getJavaBySqlType(pk.getType())),
                        true,
                        true,
                        ObjectIdValueReader.reader(pk.getName())
                );
            } else {
                id = new DefaultAgIdPart(
                        attribute.getName(),
                        typeForName(attribute.getType()),
                        true,
                        true,
                        DataObjectPropertyReader.reader(attribute.getName()));
            }

            addId(id);
        }
    }

    protected void buildAnnotatedProperties() {

        // Load a separate entity built purely from annotations, then merge it with our entity. We are not cloning
        // attributes or relationship during merge... they have no references to parent and can be used as is.

        // Note that overriding Cayenne attributes with annotated attributes will have a slight side effect -
        // DataObjectPropertyReader will be replaced with getter-based reader. Those two should behave exactly
        // the same, possibly with some really minor performance difference

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

    /**
     * @since 4.8
     */
    protected AgEntity<T> buildEntity() {
        buildCayenneEntity();
        buildAnnotatedProperties();

        return new DefaultAgEntity<>(
                cayenneEntity.getName(),
                type,
                ids,
                attributes,
                relationships,
                rootDataResolver != null ? rootDataResolver : ThrowingRootDataResolver.getInstance(),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter());
    }

    /**
     * @since 4.8
     */
    protected AgEntity<T> applyOverlay(AgEntity<T> entity) {
        return overlay != null ? overlay.resolve(agDataMap, entity) : entity;
    }
}
