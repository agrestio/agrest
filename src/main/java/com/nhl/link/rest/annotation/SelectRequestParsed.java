package com.nhl.link.rest.annotation;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nhl.link.rest.runtime.processor.select.ParseSelectRequestStage;

/**
 * A chain listener annotation for methods to be invoked after
 * {@link ParseSelectRequestStage} execution.
 * 
 * @since 1.19
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Inherited
public @interface SelectRequestParsed {

}
