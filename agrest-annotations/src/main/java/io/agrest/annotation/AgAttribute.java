package io.agrest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a getter of a property in a Java object to indicate that the property is an attribute exposed via Agrest.
 *
 * @since 1.15
 */
@Target({METHOD})
@Retention(RUNTIME)
public @interface AgAttribute {

    /**
     * @since 4.7
     */
    boolean readable() default true;

    /**
     * @since 4.7
     */
    boolean writable() default true;
}
