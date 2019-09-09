package io.agrest.meta;

import io.agrest.ResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.meta.compiler.BeanAnalyzer;
import io.agrest.meta.compiler.PropertyGetter;
import io.agrest.property.BeanPropertyReader;
import io.agrest.property.DefaultIdReader;
import io.agrest.property.PropertyReader;
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
import java.util.function.Function;

/**
 * A helper class to compile custom {@link AgEntity} objects based on annotations.
 *
 * @since 1.12
 */
public class AgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityBuilder.class);

    private Class<T> type;
    private String name;
    private AgDataMap dataMap;
    private AgEntityOverlay<T> overlay;

    private Map<String, io.agrest.meta.AgAttribute> ids;
    private Map<String, io.agrest.meta.AgAttribute> attributes;
    private Map<String, io.agrest.meta.AgRelationship> relationships;

    public AgEntityBuilder(Class<T> type, AgDataMap dataMap) {
        this.type = type;
        this.name = type.getSimpleName();
        this.dataMap = dataMap;

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public AgEntityBuilder<T> overlay(AgEntityOverlay<T> overlay) {
        this.overlay = overlay;
        return this;
    }

    public DefaultAgEntity<T> build() {

        collectProperties();
        loadOverlays();

        return new DefaultAgEntity<>(
                name,
                type,
                ids,
                attributes,
                relationships,
                new DefaultIdReader(ids.keySet()));
    }

    private void addId(io.agrest.meta.AgAttribute id) {
        ids.put(id.getName(), id);
    }

    private void addAttribute(io.agrest.meta.AgAttribute a) {
        attributes.put(a.getName(), a);
    }

    private void addRelationship(io.agrest.meta.AgRelationship r) {
        relationships.put(r.getName(), r);
    }

    private void collectProperties() {
        BeanAnalyzer.findGetters(type).forEach(getter -> appendProperty(getter));
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

        if (m.getAnnotation(AgAttribute.class) != null) {

            if (checkValidAttributeType(type, m.getGenericReturnType())) {
                addAttribute(new DefaultAgAttribute(name, type, BeanPropertyReader.reader()));
            } else {
                // still return true after validation failure... this is an attribute, just not a proper one
                LOGGER.warn("Invalid attribute type for " + this.name + "." + name + ". Skipping.");
            }

            return true;
        }

        if (m.getAnnotation(AgId.class) != null) {

            if (checkValidIdType(type)) {
                addId(new DefaultAgAttribute(name, type, BeanPropertyReader.reader()));
            } else {
                // still return true after validation failure... this is an
                // attribute, just not a proper one
                LOGGER.warn("Invalid ID attribute type for " + this.name + "." + name + ". Skipping.");
            }

            return true;
        }

        return false;
    }

    private boolean checkValidAttributeType(Class<?> type, Type genericType) {
        if (Void.class.equals(type) || void.class.equals(type) || Map.class.isAssignableFrom(type)) {
            return false;
        }
        if (Collection.class.isAssignableFrom(type) && !isCollectionOfSimpleType(type, genericType)) {
            return false;
        }
        return true;
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
        if (m.getAnnotation(AgRelationship.class) != null) {

            boolean toMany = false;
            Class<?> targetType = getter.getType();

            if (Collection.class.isAssignableFrom(targetType)) {
                targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                toMany = true;
            }

            String name = getter.getName();
            AgEntity<?> targetEntity = dataMap.getEntity(targetType);

            // unlike Cayenne entity, for POJOs read children from the object, not from the entity..

            // TODO: a decision whether to read results from the object or from the child entity (via
            //  ChildEntityResultReader and ChildEntityListResultReader) should not be dependent on the object nature,
            //  but rather on the data retrieval strategy for a given relationship

            Function<ResourceEntity<?>, PropertyReader> readerFactory = e -> BeanPropertyReader.reader();

            addRelationship(new DefaultAgRelationship(name, targetEntity, toMany, readerFactory));
        }

        return false;
    }

    protected void loadOverlays() {
        if (overlay != null) {
            overlay.getAttributes().forEach(this::addAttribute);
            overlay.getRelationships(dataMap).forEach(this::addRelationship);
        }
    }
}
