package io.agrest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a getter of a property in a POJO or a record component to indicate that the property is a "relationship"
 * in the Agrest schema, and set it default access rules. The property type should be either another entity, or a
 * collection of entities.
 * 
 * @since 1.15
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface AgRelationship {
    /**
     * @since 4.7
     */
    boolean readable() default true;

    /**
     * @since 4.7
     */
    boolean writable() default true;
}
