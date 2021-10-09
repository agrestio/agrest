package io.agrest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that web method is an Agrest resource.
 *
 * @since 2.10
 * @deprecated since 4.7, as Agrest now integrates with OpenAPI 3 / Swagger
 */
@Deprecated
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AgResource {
    /**
     * @return Class of Agrest entity that this resource works with.
     */
    Class<?> entityClass() default Object.class;

    /**
     * @return type of this resource. If not set then type is {@link LinkType#UNDEFINED}
     */
    LinkType type() default LinkType.UNDEFINED;
}
