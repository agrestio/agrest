package io.agrest.jpa.compiler;

import java.lang.reflect.Field;

import io.agrest.AgException;
import io.agrest.jpa.pocessor.JpaUtil;
import io.agrest.reader.DataReader;
import jakarta.persistence.metamodel.SingularAttribute;

/**
 * @since 5.0
 */
public class JpaPropertyReader {

    public static DataReader reader(SingularAttribute<?, ?> attribute) {
        Class<?> declaringClass = attribute.getJavaMember().getDeclaringClass();
        Class<?> entityClass = attribute.getDeclaringType().getJavaType();

        if(!entityClass.equals(declaringClass)) {
            Field entityField;
            try {
                entityField = entityClass.getDeclaredField(attribute.getJavaMember().getName());
                entityField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                throw AgException.internalServerError("No id field %s in type %s for a corresponding IdClass field found.",
                        attribute.getJavaMember().getName(), entityClass.getSimpleName());
            }

            return o -> {
                if(o == null) {
                    throw new NullPointerException("Can't read attribute on a null value.");
                }
                Class<?> actualClass = o.getClass();
                if(actualClass.equals(entityClass)) {
                    return JpaUtil.readProperty(o, entityField);
                } else {
                    return JpaUtil.readProperty(o, attribute);
                }
            };
        } else {
            return o -> JpaUtil.readProperty(o, attribute);
        }
    }

}
