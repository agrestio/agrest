package com.nhl.link.rest.annotation.listener;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nhl.link.rest.runtime.cayenne.processor.CayenneUpdatePostProcessStage;

/**
 * A chain listener annotation for methods to be invoked after
 * {@link CayenneUpdatePostProcessStage} that forms the update response.
 * 
 * @since 1.19
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Inherited
public @interface UpdateResponseUpdated {

}
