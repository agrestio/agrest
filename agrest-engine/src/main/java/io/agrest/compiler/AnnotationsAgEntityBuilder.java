package io.agrest.compiler;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.ReadFilter;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.reflect.BeanAnalyzer;
import io.agrest.reflect.PropertyGetter;
import io.agrest.meta.AgSchema;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.meta.AgIdPart;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.DefaultAgEntity;
import io.agrest.meta.DefaultAgIdPart;
import io.agrest.meta.DefaultAgRelationship;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to compile custom {@link AgEntity} objects based on annotations.
 *
 * @since 4.1
 */
public class AnnotationsAgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsAgEntityBuilder.class);

    private final Class<T> type;
    private final String name;
    private final AgSchema schema;
    private AgEntityOverlay<T> overlay;
    private RootDataResolver<T> rootDataResolver;

    private final Map<String, AgIdPart> ids;
    private final Map<String, io.agrest.meta.AgAttribute> attributes;
    private final Map<String, io.agrest.meta.AgRelationship> relationships;

    public AnnotationsAgEntityBuilder(Class<T> type, AgSchema schema) {
        this.type = type;
        this.name = type.getSimpleName();
        this.schema = schema;

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public AnnotationsAgEntityBuilder<T> overlay(AgEntityOverlay<T> overlay) {
        this.overlay = overlay;
        return this;
    }

    public AnnotationsAgEntityBuilder<T> rootDataResolver(RootDataResolver<T> resolver) {
        this.rootDataResolver = resolver;
        return this;
    }

    public AgEntity<T> build() {
        return applyOverlay(buildEntity());
    }

    private void addId(AgIdPart id) {
        ids.put(id.getName(), id);
    }

    private void addAttribute(io.agrest.meta.AgAttribute a) {
        attributes.put(a.getName(), a);
    }

    private void addRelationship(io.agrest.meta.AgRelationship r) {
        relationships.put(r.getName(), r);
    }

    private void collectProperties() {
        BeanAnalyzer.findGetters(type).forEach(this::appendProperty);
    }

    private void appendProperty(PropertyGetter getter) {

        if (!addAsAttribute(getter)) {
            addAsRelationship(getter);
        }
    }

    private boolean addAsAttribute(PropertyGetter getter) {

        Method m = getter.getMethod();
        Class<?> type = getter.getType();
        String name = getter.getName();

        AgAttribute aAtt = m.getAnnotation(AgAttribute.class);
        if (aAtt != null) {

            if (checkValidAttributeType(type, m.getGenericReturnType())) {
                addAttribute(new DefaultAgAttribute(name, type, aAtt.readable(), aAtt.writable(), getter::getValue));
            } else {
                // still return true after validation failure... this is an attribute, just not a proper one
                LOGGER.warn("Invalid attribute type for " + this.name + "." + name + ". Skipping.");
            }

            return true;
        }

        AgId aId = m.getAnnotation(AgId.class);
        if (aId != null) {

            if (checkValidIdType(type)) {
                addId(new DefaultAgIdPart(name, type, aId.readable(), aId.writable(), getter::getValue));
            } else {
                // still return true after validation failure... this is an attribute, just not a proper one
                LOGGER.warn("Invalid ID attribute type for " + this.name + "." + name + ". Skipping.");
            }

            return true;
        }

        return false;
    }

    private boolean checkValidAttributeType(Class<?> type, Type genericType) {
        return !Void.class.equals(type)
                && !void.class.equals(type)
                && !Map.class.isAssignableFrom(type)
                && (!Collection.class.isAssignableFrom(type) || isCollectionOfSimpleType(type, genericType));
    }

    private boolean isCollectionOfSimpleType(Class<?> type, Type genericType) {
        if (Collection.class.isAssignableFrom(type) && genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type[] typeArgs = pt.getActualTypeArguments();
            if (typeArgs.length == 1) {
                return isSimpleType(typeArgs[0]);
            }
        }
        return false;
    }

    private boolean isSimpleType(Type rawType) {
        Class<?> cls = null;
        if (rawType instanceof Class) {
            cls = (Class<?>) rawType;
        } else if (rawType instanceof WildcardType) {
            Type[] bounds = ((WildcardType) rawType).getUpperBounds();
            if (bounds.length == 1 && bounds[0] instanceof Class) {
                cls = (Class<?>) bounds[0];
            }
        } else if (rawType instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable) rawType).getBounds();
            if (bounds.length == 1 && bounds[0] instanceof Class) {
                cls = (Class<?>) bounds[0];
            }
        }
        if (cls != null) {
            return String.class.isAssignableFrom(cls)
                    || Number.class.isAssignableFrom(cls)
                    || Boolean.class.isAssignableFrom(cls)
                    || Character.class.isAssignableFrom(cls);
        }
        return false;
    }

    private boolean checkValidIdType(Class<?> type) {
        return !Void.class.equals(type) && !void.class.equals(type) && !Map.class.isAssignableFrom(type)
                && !Collection.class.isAssignableFrom(type);
    }

    private boolean addAsRelationship(PropertyGetter getter) {

        Method m = getter.getMethod();
        AgRelationship aRel = m.getAnnotation(AgRelationship.class);

        if (aRel != null) {

            boolean toMany = false;
            Class<?> targetType = getter.getType();

            if (Collection.class.isAssignableFrom(targetType)) {
                targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                toMany = true;
            }

            addRelationship(new DefaultAgRelationship(
                    getter.getName(),
                    schema.getEntity(targetType),
                    toMany,
                    aRel.readable(),
                    aRel.writable(),
                    new ReaderBasedResolver<>(getter::getValue))
            );
        }

        return false;
    }

    /**
     * @since 4.8
     */
    protected AgEntity<T> buildEntity() {
        collectProperties();
        return new DefaultAgEntity<>(
                name,
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
        return overlay != null ? overlay.resolve(schema, entity) : entity;
    }
}
