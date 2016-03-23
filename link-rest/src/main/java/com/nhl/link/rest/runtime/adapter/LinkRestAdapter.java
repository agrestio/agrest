package com.nhl.link.rest.runtime.adapter;

import java.util.Collection;

import javax.ws.rs.core.Feature;

import org.apache.cayenne.di.Binder;

/**
 * Defines interface of an "adapter" that customizes default LinkRest runtime
 * for a specific flavor of REST interactions. Adapter interface is very generic
 * and allows to customize all aspects of LinkRest accessible via DI.
 * 
 * @since 1.3
 */
public interface LinkRestAdapter {

	void contributeToRuntime(Binder binder);

	void contributeToJaxRs(Collection<Feature> features);
}
