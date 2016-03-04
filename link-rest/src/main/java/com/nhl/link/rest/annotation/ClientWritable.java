package com.nhl.link.rest.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Defines a set of properties for a given entity that can be modified by the
 * client.
 * 
 * @since 1.5
 */
@Target({ TYPE })
@Retention(RUNTIME)
public @interface ClientWritable {

	String[] value();

	boolean id() default false;
}
