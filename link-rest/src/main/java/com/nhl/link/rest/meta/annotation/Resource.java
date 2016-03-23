package com.nhl.link.rest.meta.annotation;

import com.nhl.link.rest.meta.LinkType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to indicate that web method is an LR resource.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Resource {

    /**
     * @return Class of LR entity that this resource works with.
     */
    Class<?> entityClass() default Object.class;

    /**
     * @return type of this resource. If not set then type is {@link com.nhl.link.rest.meta.LinkType#UNDEFINED}.
     */
    LinkType type() default LinkType.UNDEFINED;

}
