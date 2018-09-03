package io.agrest.meta;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.meta.compiler.BeanAnalyzer;
import io.agrest.meta.compiler.PropertyGetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.Map;

/**
 * A helper class to compile custom {@link AgEntity} objects based on annotations.
 *
 * @since 1.12
 */
public class AgEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgEntityBuilder.class);

    private Class<T> type;
    private AgDataMap dataMap;

    public AgEntityBuilder(Class<T> type, AgDataMap dataMap) {
        this.type = type;
        this.dataMap = dataMap;
    }

    public DefaultAgEntity<T> build() {
        DefaultAgEntity<T> e = new DefaultAgEntity<>(type);
        appendProperties(e);
        return e;
    }

    private void appendProperties(DefaultAgEntity<T> entity) {
        BeanAnalyzer.findGetters(type).forEach(getter -> appendProperty(entity, getter));
    }

    private void appendProperty(DefaultAgEntity<T> entity, PropertyGetter getter) {
        if (!addAsAttribute(entity, getter)) {
            addAsRelationship(entity, getter);
        }
    }

    private boolean addAsAttribute(DefaultAgEntity<T> entity, PropertyGetter getter) {

        Method m = getter.getMethod();
        Class<?> type = getter.getType();
        String name = getter.getName();

        if (m.getAnnotation(AgAttribute.class) != null) {

            if (checkValidAttributeType(type, m.getGenericReturnType())) {
                DefaultAgAttribute a = new DefaultAgAttribute(name, type);
                entity.addAttribute(a);
            } else {
                // still return true after validation failure... this is an
                // attribute, just not a proper one
                LOGGER.warn("Invalid attribute type for " + entity.getName() + "." + name + ". Skipping.");
            }

            return true;
        }

        if (m.getAnnotation(AgId.class) != null) {

            if (checkValidIdType(type)) {
                DefaultAgAttribute a = new DefaultAgAttribute(name, type);
                entity.addId(a);
            } else {
                // still return true after validation failure... this is an
                // attribute, just not a proper one
                LOGGER.warn("Invalid ID attribute type for " + entity.getName() + "." + name + ". Skipping.");
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

    private boolean addAsRelationship(DefaultAgEntity<T> entity, PropertyGetter getter) {

        Method m = getter.getMethod();
        Class<?> targetType = getter.getType();
        String name = getter.getName();

        if (m.getAnnotation(AgRelationship.class) != null) {

            boolean toMany = false;

            if (Collection.class.isAssignableFrom(targetType)) {
                targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                toMany = true;
            }

            AgEntity<?> targetEntity = dataMap.getEntity(targetType);
            entity.addRelationship(new DefaultAgRelationship(name, targetEntity, toMany));
        }

        return false;
    }
}
