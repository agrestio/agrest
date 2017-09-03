package com.nhl.link.rest.meta;

import com.nhl.link.rest.annotation.LrAttribute;
import com.nhl.link.rest.annotation.LrId;
import com.nhl.link.rest.annotation.LrRelationship;
import com.nhl.link.rest.meta.compiler.BeanAnalyzer;
import com.nhl.link.rest.meta.compiler.PropertyMethod;
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
 * A helper class to compile custom {@link LrEntity} objects based on annotations.
 *
 * @since 1.12
 */
public class LrEntityBuilder<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LrEntityBuilder.class);

    private Class<T> type;
    private LrDataMap dataMap;

    public LrEntityBuilder(Class<T> type, LrDataMap dataMap) {
        this.type = type;
        this.dataMap = dataMap;
    }

    public DefaultLrEntity<T> build() {
        DefaultLrEntity<T> e = new DefaultLrEntity<>(type);
        appendProperties(e);
        return e;
    }

    private void appendProperties(DefaultLrEntity<T> entity) {
        BeanAnalyzer.findGetters(type).forEach(getter -> appendProperty(entity, getter));
    }

    private void appendProperty(DefaultLrEntity<T> entity, PropertyMethod getter) {
        if (!addAsAttribute(entity, getter)) {
            addAsRelationship(entity, getter);
        }
    }

    private boolean addAsAttribute(DefaultLrEntity<T> entity, PropertyMethod getter) {

        Method m = getter.getGetterOrSetter();
        Class<?> type = getter.getType();
        String name = getter.getName();

        if (m.getAnnotation(LrAttribute.class) != null) {

            if (checkValidAttributeType(type, m.getGenericReturnType())) {
                DefaultLrAttribute a = new DefaultLrAttribute(name, type);
                entity.addAttribute(a);
            } else {
                // still return true after validation failure... this is an
                // attribute, just not a proper one
                LOGGER.warn("Invalid attribute type for " + entity.getName() + "." + name + ". Skipping.");
            }

            return true;
        }

        if (m.getAnnotation(LrId.class) != null) {

            if (checkValidIdType(type)) {
                DefaultLrAttribute a = new DefaultLrAttribute(name, type);
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

    private boolean addAsRelationship(DefaultLrEntity<T> entity, PropertyMethod getter) {

        Method m = getter.getGetterOrSetter();
        Class<?> targetType = getter.getType();
        String name = getter.getName();

        if (m.getAnnotation(LrRelationship.class) != null) {

            boolean toMany = false;

            if (Collection.class.isAssignableFrom(targetType)) {
                targetType = (Class<?>) ((ParameterizedType) m.getGenericReturnType()).getActualTypeArguments()[0];
                toMany = true;
            }

            LrEntity<?> targetEntity = dataMap.getEntity(targetType);
            entity.addRelationship(new DefaultLrRelationship(name, targetEntity, toMany));
        }

        return false;
    }
}
