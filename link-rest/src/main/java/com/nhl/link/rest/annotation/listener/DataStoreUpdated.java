package com.nhl.link.rest.annotation.listener;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.nhl.link.rest.runtime.cayenne.processor.CayenneFullSyncStage;
import com.nhl.link.rest.runtime.cayenne.processor.CayenneUpdateStage;

/**
 * A chain listener annotation for methods to be invoked after
 * {@link CayenneUpdateStage}, {@link CayenneFullSyncStage}, and similar update
 * stages that generate and execute update operations. Note that any changes to
 * objects are already committed to the data store when this is invoked.
 * 
 * @since 1.19
 */
@Target({ METHOD })
@Retention(RUNTIME)
@Inherited
public @interface DataStoreUpdated {

}
