package io.agrest.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotates a getter of a property in a POJO to indicate that the property is a Agrest-exposed attribute.
 * 
 * @since 1.15
 */
@Target({ METHOD })
@Retention(RUNTIME)
public @interface AgAttribute {

}
