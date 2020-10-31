package io.agrest.compiler;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.base.reflect.BeanAnalyzer;
import io.agrest.base.reflect.PropertyGetter;
import io.agrest.meta.*;
import io.agrest.resolver.ReaderBasedResolver;
import io.agrest.resolver.RootDataResolver;
import io.agrest.resolver.ThrowingRootDataResolver;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A helper class to compile custom {@link AgEntity} objects based on annotations.
 *
 * @since 1.12
 */
public class AgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityBuilder.class);

    private final Class<T> type;
    private final String name;
    private final AgDataMap agDataMap;
    private AgEntityOverlay<T> overlay;
    private RootDataResolver<T> rootDataResolver;

    private final Map<String, AgIdPart> ids;
    private final Map<String, io.agrest.meta.AgAttribute> attributes;
    private final Map<String, io.agrest.meta.AgRelationship> relationships;

    public AgEntityBuilder(Class<T> type, AgDataMap agDataMap) {
        this.type = type;
        this.name = type.getSimpleName();
        this.agDataMap = agDataMap;

        this.ids = new HashMap<>();
        this.attributes = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public AgEntityBuilder<T> overlay(AgEntityOverlay<T> overlay) {
        this.overlay = overlay;
        return this;
    }

    /**
     * @since 3.4
     */
    public AgEntityBuilder<T> rootDataResolver(RootDataResolver<T> resolver) {
        this.rootDataResolver = resolver;
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
                rootDataResolver != null ? rootDataResolver : ThrowingRootDataResolver.getInstance());
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

        if (m.getAnnotation(AgAttribute.class) != null) {

            if (checkValidAttributeType(type, m.getGenericReturnType())) {
                addAttribute(new DefaultAgAttribute(name, type, getter::getValue));
            } else {
                // still return true after validation failure... this is an attribute, just not a proper one
                LOGGER.warn("Invalid attribute type for " + this.name + "." + name + ". Skipping.");
            }

            return true;
        }

        if (m.getAnnotation(AgId.class) != null) {

            if (checkValidIdType(type)) {
                addId(new DefaultAgIdPart(name, type, getter::getValue, new ASTObjPath(name)));
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
        if (m.getAnnotation(AgRelationship.class) != null) {

            boolean toMany = false;
            Class<?> targetType = getter.getType();

            if (Collection.class.isAssignableFrom(targetType)) {
                targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                toMany = true;
            }

            addRelationship(new DefaultAgRelationship(
                    getter.getName(),
                    agDataMap.getEntity(targetType),
                    toMany,
                    new ReaderBasedResolver<>(getter::getValue))
            );
        }

        return false;
    }

    protected void loadOverlays() {
        if (overlay != null) {
            overlay.getAttributes().forEach(this::addAttribute);
            overlay.getRelationshipOverlays().forEach(this::loadRelationshipOverlay);
            overlay.getExcludes().forEach(this::removeIdOrAttributeOrRelationship);

            if (overlay.getRootDataResolver() != null) {
                this.rootDataResolver = overlay.getRootDataResolver();
            }
        }
    }

    protected void loadRelationshipOverlay(AgRelationshipOverlay overlay) {
        io.agrest.meta.AgRelationship relationship = overlay.resolve(relationships.get(overlay.getName()), agDataMap);
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
