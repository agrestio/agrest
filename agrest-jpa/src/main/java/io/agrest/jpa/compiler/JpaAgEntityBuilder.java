package io.agrest.jpa.compiler;

import java.util.HashMap;
import java.util.Map;

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
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import jakarta.persistence.metamodel.PluralAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 5.0
 */
public class JpaAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaAgEntityBuilder.class);

    private final Class<T> type;
    private final AgDataMap agDataMap;
    private final EntityType<T> jpaEntity;
    private final Map<String, AgIdPart> ids;
    private final Map<String, AgAttribute> attributes;
    private final Map<String, AgRelationship> relationships;

    private AgEntityOverlay<T> overlay;
    private RootDataResolver<T> rootDataResolver;
    private NestedDataResolver<T> nestedDataResolver;

    public JpaAgEntityBuilder(Class<T> type, AgDataMap agDataMap, Metamodel metamodel) {

        this.type = type;
        this.agDataMap = agDataMap;
        this.jpaEntity = metamodel.entity(type);

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public JpaAgEntityBuilder<T> overlay(AgEntityOverlay<T> overlay) {
        this.overlay = overlay;
        return this;
    }

    public JpaAgEntityBuilder<T> rootDataResolver(RootDataResolver<T> resolver) {
        this.rootDataResolver = resolver;
        return this;
    }

    public JpaAgEntityBuilder<T> nestedDataResolver(NestedDataResolver<T> resolver) {
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

    protected void buildFromJpaEntity() {

        for (SingularAttribute<?, ?> attribute : jpaEntity.getSingularAttributes()) {
            if(attribute.isId()) {
                addId(new DefaultAgIdPart(
                        attribute.getName(),
                        attribute.getJavaType(),
                        true,
                        true,
                        JpaPropertyReader.reader(attribute))
                );
                if(attribute.getName().equals("id")) {
                    continue;
                }
            }
            Class<?> type = attribute.getJavaType();
            String name = attribute.getName();

            if(attribute.isAssociation()) {
                // to-one
                addRelationship(new DefaultAgRelationship(
                        name,
                        // 'agDataMap.getEntity' will compile the entity on the fly if needed
                        agDataMap.getEntity(type),
                        false,
                        true,
                        true,
                        nestedDataResolver));
            } else {
                // by default adding attributes as readable and writable... @AgAttribute annotation on a getter may override this
                addAttribute(new DefaultAgAttribute(name, type, true, true, JpaPropertyReader.reader(attribute)));
            }
        }

        for (PluralAttribute<?, ?, ?> attribute : jpaEntity.getPluralAttributes()) {
            if(!attribute.isAssociation()) {
                // TODO: should we map collection-based attributes to AgRest?
                continue;
            }
            Type<?> elementType = attribute.getElementType();
            Class<?> targetEntityType = elementType.getJavaType();
            boolean toMany = attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.MANY_TO_MANY
                    || attribute.getPersistentAttributeType() == Attribute.PersistentAttributeType.ONE_TO_MANY;

            addRelationship(new DefaultAgRelationship(
                    attribute.getName(),
                    // 'agDataMap.getEntity' will compile the entity on the fly if needed
                    agDataMap.getEntity(targetEntityType),
                    toMany,
                    true,
                    true,
                    nestedDataResolver));
        }
    }

    protected void buildAnnotatedProperties() {

        // Load a separate entity built purely from annotations, then merge it with our entity. We are not cloning
        // attributes or relationship during merge... they have no references to parent and can be used as is.

        // Note that overriding JPA attributes with annotated attributes will have a slight side effect -
        // DataObjectPropertyReader will be replaced with getter-based reader. Those two should behave exactly
        // the same, possibly with some really minor performance difference

        AgEntity<T> annotatedEntity = new AnnotationsAgEntityBuilder<>(type, agDataMap).build();

        if (annotatedEntity.getIdParts().size() > 0) {

            // if annotated ids are present, remove all JPA-originated ids, regardless of their type and count
            if (!ids.isEmpty()) {
                LOGGER.debug("Hibernate Id is overridden from annotations.");
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

    protected AgEntity<T> buildEntity() {
        buildFromJpaEntity();
        buildAnnotatedProperties();

        return new DefaultAgEntity<>(
                jpaEntity.getName(),
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

    protected AgEntity<T> applyOverlay(AgEntity<T> entity) {
        return overlay != null ? overlay.resolve(agDataMap, entity) : entity;
    }
}
