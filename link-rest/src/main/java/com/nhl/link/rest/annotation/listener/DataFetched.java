package com.nhl.link.rest.annotation.listener;

import com.nhl.link.rest.runtime.cayenne.processor.CayenneFetchStage;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A chain listener annotation for methods to be invoked after
 * {@link CayenneFetchStage} or another fetch stage execution.
 * 
 * @since 1.19
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Inherited
public @interface DataFetched {
}
