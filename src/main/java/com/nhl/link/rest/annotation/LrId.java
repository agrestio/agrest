package com.nhl.link.rest.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotates a getter of a property in a POJO to indicate that the property is a
 * LinkRest-exposed attribute.
 * 
 * @since 1.15
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface LrId {

}
