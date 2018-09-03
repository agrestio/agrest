package io.agrest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that web method is an AgREST resource.
 *
 * @since 2.10
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgResource {
    /**
     * @return Class of AgREST entity that this resource works with.
     */
    Class<?> entityClass() default Object.class;

    /**
     * @return type of this resource. If not set then type is {@link LinkType#UNDEFINED}
     */
    LinkType type() default LinkType.UNDEFINED;
}
