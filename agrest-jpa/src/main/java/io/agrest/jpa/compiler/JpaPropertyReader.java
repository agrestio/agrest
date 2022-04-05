package io.agrest.jpa.compiler;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import io.agrest.property.PropertyReader;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @since 5.0
 */
public class JpaPropertyReader {

    public static PropertyReader reader(SingularAttribute<?, ?> attribute) {
        Member javaMember = attribute.getJavaMember();
        if(javaMember instanceof Method) {
            return o -> safeInvoke((Method) javaMember, o);
        }
        if(javaMember instanceof Field) {
            return o -> safeInvoke((Field) javaMember, o);
        }

        throw new IllegalArgumentException("Attribute " + attribute.getName() + " doesn't have proper java member to access it's value.");
    }

    private static Object safeInvoke(Method method, Object object) {
        try {
            return method.invoke(object);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object safeInvoke(Field field, Object object) {
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
