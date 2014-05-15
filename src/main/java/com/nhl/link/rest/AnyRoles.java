package com.nhl.link.rest;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * An authorization annotation that allows to tag JAX RS resource methods with
 * application specific roles. As the name implies, it is sufficient for the
 * user to have at least one of the roles to be allowed access.
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface AnyRoles {

	String[] value();
}
