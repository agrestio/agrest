package com.nhl.link.rest.runtime.adapter.java8;

import java.util.Collection;

import javax.ws.rs.core.Feature;

import org.apache.cayenne.di.Binder;

import com.nhl.link.rest.runtime.adapter.LinkRestAdapter;

/**
 * Empty adapter, kept for binary backwards compatibility with Bootique, etc.
 * 
 * @deprecated since 2.0.
 */
public class Java8Adapter implements LinkRestAdapter {

	@Override
	public void contributeToJaxRs(Collection<Feature> features) {
		// do nothing
	}

	@Override
	public void contributeToRuntime(Binder binder) {
		// do nothing
	}
}
