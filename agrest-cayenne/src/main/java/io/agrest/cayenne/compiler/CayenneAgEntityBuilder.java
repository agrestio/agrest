package io.agrest.cayenne.compiler;

import io.agrest.AgException;
import io.agrest.PathConstants;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.compiler.AnnotationsAgEntityBuilder;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.AgRelationship;
import io.agrest.meta.AgSchema;
import io.agrest.meta.DefaultAttribute;
import io.agrest.meta.DefaultEntity;
import io.agrest.meta.DefaultIdPart;
import io.agrest.meta.DefaultRelationship;
import io.agrest.resolver.RelatedDataResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.apache.cayenne.ObjectId;
import org.apache.cayenne.dba.TypesMapping;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.EntityInheritanceTree;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.apache.cayenne.reflect.ClassDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

import static io.agrest.reflect.Types.typeForName;

/**
 * @since 3.4
 */
public class CayenneAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneAgEntityBuilder.class);

    private final EntityResolver cayenneResolver;
    private final Class<T> type;
    private final AgSchema schema;
    private final ObjEntity cayenneEntity;
    private final Collection<AgEntity<? extends T>> subEntities;
    private final Map<String, AgIdPart> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    private Map<String, AgEntityOverlay<?>> overlays;
    private RootDataResolver<T> dataResolver;
    private RelatedDataResolver<T> relatedDataResolver;

    public CayenneAgEntityBuilder(Class<T> type, AgSchema schema, EntityResolver cayenneResolver) {

        this.cayenneResolver = cayenneResolver;

        this.type = type;
        this.schema = schema;
        this.cayenneEntity = cayenneResolver.getObjEntity(type);

        this.subEntities = new HashSet<>();

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public CayenneAgEntityBuilder<T> overlays(Map<String, AgEntityOverlay<?>> overlays) {
        this.overlays = overlays;
        return this;
    }

    /**
     * @since 5.0
     */
    public CayenneAgEntityBuilder<T> dataResolver(RootDataResolver<T> dataResolver) {
        this.dataResolver = dataResolver;
        return this;
    }

    /**
     * @since 5.0
     */
    public CayenneAgEntityBuilder<T> relatedDataResolver(RelatedDataResolver<T> relatedDataResolver) {
        this.relatedDataResolver = relatedDataResolver;
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

        EntityInheritanceTree inheritanceTree = cayenneResolver.getInheritanceTree(cayenneEntity.getName());
        for (ObjEntity cayenneSubEntity : inheritanceTree.allSubEntities()) {

            // include only the direct sub-entities
            if (cayenneSubEntity.getSuperEntity() == cayenneEntity) {
                ClassDescriptor subEntityDesc = cayenneResolver.getClassDescriptor(cayenneSubEntity.getName());
                AgEntity<? extends T> subEntity = (AgEntity<? extends T>) schema.getEntity(subEntityDesc.getObjectClass());
                subEntities.add(subEntity);
            }
        }

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
            addAttribute(new DefaultAttribute(name, type, true, true, DataObjectDataReader.reader(name)));
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

            addRelationship(new DefaultRelationship(
                    r.getName(),
                    // 'schema.getEntity' will compile the entity on the fly if needed
                    schema.getEntity(targetEntityType),
                    r.isToMany(),

                    // TODO: maybe this should be "false, false" by default, giving us a default request model
                    //  (i.e. all attributes, no relationships)
                    true,
                    true,
                    relatedDataResolver));
        }

        for (DbAttribute pk : cayenneEntity.getDbEntity().getPrimaryKeys()) {
            ObjAttribute attribute = cayenneEntity.getAttributeForDbAttribute(pk);

            AgIdPart id;
            if (attribute == null) {

                id = new DefaultIdPart(
                        // TODO: we are exposing DB column name here
                        ASTDbPath.DB_PREFIX + pk.getName(),
                        typeForName(TypesMapping.getJavaBySqlType(pk.getType())),
                        true,
                        true,
                        ObjectIdValueReader.reader(pk.getName())
                );
            } else {
                id = new DefaultIdPart(
                        attribute.getName(),
                        typeForName(attribute.getType()),
                        true,
                        true,
                        DataObjectDataReader.reader(attribute.getName()));
            }

            addId(id);
        }
    }

    protected void buildAnnotatedProperties() {

        // Load a separate entity built purely from annotations, then merge it with our entity.

        AgEntity<T> annotatedEntity = new AnnotationsAgEntityBuilder<>(type, schema).build();
        Collection<AgIdPart> annotatedIdParts = annotatedEntity.getIdParts();

        if (!annotatedIdParts.isEmpty()) {

            // TODO: we should remove a possible matching regular persistent attributes from the model, since it was
            //  also declared as ID, and hence should not be exposed as an attribute anymore

            switch (IdMergeType.of(cayenneEntity.getName(), annotatedIdParts)) {
                case replace:
                    replaceIds(annotatedIdParts);
                    break;
                case merge_single:
                    mergeSingleId(annotatedIdParts.iterator().next());
                    break;
            }
        }

        for (AgAttribute attribute : annotatedEntity.getAttributes()) {
            AgAttribute existing = attributes.get(attribute.getName());
            if (existing != null) {
                LOGGER.debug("Attribute '{}' is overridden from annotations.", existing.getName());
                addAttribute(merge(attribute, existing));
            } else {
                addAttribute(attribute);
            }
        }

        for (AgRelationship relationship : annotatedEntity.getRelationships()) {

            AgRelationship existing = relationships.get(relationship.getName());
            if (existing != null) {
                LOGGER.debug("Relationship '{}' is overridden from annotations.", existing.getName());
                addRelationship(merge(relationship, existing));
            } else {
                addRelationship(relationship);
            }
        }
    }

    protected void replaceIds(Collection<AgIdPart> annotatedIds) {
        ids.clear();
        annotatedIds.forEach(this::addId);
    }

    protected void mergeSingleId(AgIdPart annotatedId) {

        if (this.ids.isEmpty()) {
            return;
        }

        // merge read/write access to the existing IDs
        // we can't iterate and change the ids collection. So iterate by copy
        for (AgIdPart id : new ArrayList<>(this.ids.values())) {
            addId(merge(annotatedId, id));
        }
    }

    protected AgIdPart merge(AgIdPart annotatedId, AgIdPart cayenneId) {
        // TODO: use Overlays here.. Overlays are intended for merging on top of entities
        return new DefaultIdPart(
                cayenneId.getName(),
                cayenneId.getType(),

                // only read/write access can be overridden by annotations as of now
                annotatedId.isReadable(),
                annotatedId.isWritable(),

                cayenneId.getDataReader());
    }

    protected AgAttribute merge(AgAttribute annotatedAttribute, AgAttribute cayenneAttribute) {
        // TODO: use Overlays here.. Overlays are intended for merging on top of entities
        return new DefaultAttribute(
                cayenneAttribute.getName(),
                cayenneAttribute.getType(),

                // only read/write access can be overridden by annotations as of now
                annotatedAttribute.isReadable(),
                annotatedAttribute.isWritable(),

                cayenneAttribute.getDataReader());
    }

    protected AgRelationship merge(AgRelationship annotatedRelationship, AgRelationship cayenneRelationship) {
        // TODO: use Overlays here.. Overlays are intended for merging on top of entities
        return new DefaultRelationship(
                cayenneRelationship.getName(),
                cayenneRelationship.getTargetEntity(),
                cayenneRelationship.isToMany(),

                // only read/write access can be overridden by annotations as of now
                annotatedRelationship.isReadable(),
                annotatedRelationship.isWritable(),

                cayenneRelationship.getDataResolver()
        );
    }

    /**
     * @since 4.8
     */
    protected AgEntity<T> buildEntity() {
        buildCayenneEntity();
        buildAnnotatedProperties();

        return new DefaultEntity<>(
                cayenneEntity.getName(),
                type,
                cayenneEntity.isAbstract(),
                subEntities,
                ids,
                attributes,
                relationships,
                dataResolver != null ? dataResolver : ThrowingRootDataResolver.getInstance(),
                ReadFilter.allowsAllFilter(),
                CreateAuthorizer.allowsAllFilter(),
                UpdateAuthorizer.allowsAllFilter(),
                DeleteAuthorizer.allowsAllFilter());
    }

    /**
     * @since 4.8
     */
    protected AgEntity<T> applyOverlay(AgEntity<T> entity) {
        return entity.resolveOverlayHierarchy(schema, entityOverlayMap());
    }

    private Map<Class<?>, AgEntityOverlay<?>> entityOverlayMap() {
        // TODO: inefficiency - Cayenne DI MapBuilder only supports String keys, so we convert a Class to a String
        //  in the runtime builder, and then convert the String back to the Class here

        if (overlays == null || overlays.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Class<?>, AgEntityOverlay<?>> byClass = new HashMap<>();

        AgEntityOverlay<?> rootOverlay = overlays.get(type.getName());
        if (rootOverlay != null) {
            byClass.put(type, rootOverlay);
        }

        for (AgEntity<?> se : subEntities) {
            AgEntityOverlay<?> seOverlay = overlays.get(se.getType().getName());
            if (seOverlay != null) {
                byClass.put(seOverlay.getType(), seOverlay);
            }
        }

        return byClass;
    }

    enum IdMergeType {
        replace, merge_single;

        static IdMergeType of(String entityName, Collection<AgIdPart> annotatedIds) {

            if (annotatedIds.size() == 1) {
                AgIdPart part = annotatedIds.iterator().next();
                return isCayenneObjectId(part) ? merge_single : replace;
            }

            for (AgIdPart p : annotatedIds) {
                if (isCayenneObjectId(p)) {
                    throw AgException.internalServerError(
                            "Entity '%s': unsupported combination of @AgId-annotated properties - %s (both 'objectId' and custom properties)",
                            entityName,
                            annotatedIds.stream().map(AgIdPart::getName).collect(Collectors.joining(",")));
                }
            }

            return replace;
        }

        static boolean isCayenneObjectId(AgIdPart part) {
            return "objectId".equals(part.getName()) && ObjectId.class.equals(part.getType());
        }
    }
}
