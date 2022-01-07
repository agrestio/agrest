package io.agrest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a getter of a property in an object to indicate that it is the entity "id". Multiple methods can be
 * annotated with "@AgId" in a given entity if a unique identifier of the object id is made of multiple properties.
 * 
 * @since 1.15
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface AgId {

    /**
     * @since 4.7
     */
    boolean readable() default true;

    /**
     * @since 4.7
     */
    boolean writable() default true;
}
