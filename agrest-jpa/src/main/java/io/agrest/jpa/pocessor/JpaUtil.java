package io.agrest.jpa.pocessor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import io.agrest.AgException;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.PluralAttribute;

/**
 * @since 5.0
 */
public class JpaUtil {


    private static Object safeInvoke(Method method, Object object, Object... value) {
        try {
            return method.invoke(object, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static void safeSet(Field field, Object object, Object value) {
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object safeGet(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void setToOneTarget(Object parent, Attribute<?, ?> attribute, Object object) {
        writeProperty(parent, attribute, object);
    }

    @SuppressWarnings("unchecked")
    public static void setToManyTarget(Object parent, PluralAttribute<?, ?, ?> attribute, Object object) {
        Object collection = readProperty(parent, attribute);
        switch (attribute.getCollectionType()) {
            case MAP:
                // TODO: how do we get the key?
                break;
            case SET:
            case LIST:
            case COLLECTION:
                ((Collection<Object>)collection).add(object);
        }
    }

    public static void removeToManyTarget(Object parent, PluralAttribute<?, ?, ?> attribute, Object object) {
        Object collection = readProperty(parent, attribute);
        switch (attribute.getCollectionType()) {
            case MAP:
                ((Map<?,?>)collection).entrySet().removeIf(e -> e.getValue() == object);
                break;
            case SET:
            case LIST:
            case COLLECTION:
                ((Collection<?>)collection).remove(object);
                break;
        }
    }

    public static Object readProperty(Object object, Attribute<?, ?> attribute) {
        return readProperty(object, attribute.getJavaMember());
    }

    public static Object readProperty(Object object, Member javaMember) {
        if(javaMember instanceof Method) {
            return safeInvoke((Method) javaMember, object);
        } else if(javaMember instanceof Field) {
            return safeGet((Field) javaMember, object);
        } else {
            throw AgException.badRequest("Can't get attribute '%s' for the entity %s",
                    javaMember.getName(),
                    javaMember.getDeclaringClass().getSimpleName());
        }
    }

    public static void writeProperty(Object object, Attribute<?, ?> attribute, Object value) {
        Member javaMember = attribute.getJavaMember();
        if(javaMember instanceof Method) {
            safeInvoke((Method) javaMember, object, value);
        } else if(javaMember instanceof Field) {
            safeSet((Field) javaMember, object, value);
        } else {
            throw AgException.badRequest("Can't set attribute '%s' for the entity %s",
                    attribute.getName(),
                    attribute.getDeclaringType().getJavaType().getName());
        }
    }
}
