package com.nhl.link.rest.runtime;

import java.util.Collection;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.di.Injector;

import com.nhl.link.rest.provider.DataResponseWriter;
import com.nhl.link.rest.provider.ResponseStatusDynamicFeature;
import com.nhl.link.rest.provider.SimpleResponseWriter;

/**
 * A JAX RS "feature" that bootstraps LinkRest in an app.
 */
class LinkRestFeature implements Feature {

	private Injector injector;
	private Collection<Class<?>> extraComponents;
	private Collection<Feature> extraFeatures;

	LinkRestFeature(Injector injector, Collection<Feature> extraFeatures, Collection<Class<?>> extraComponents) {
		this.injector = injector;
		this.extraFeatures = extraFeatures;
		this.extraComponents = extraComponents;
	}

	@Override
	public boolean configure(FeatureContext context) {

		// this gives everyone access to the LinkRest services
		context.property(LinkRestRuntime.LINK_REST_CONTAINER_PROPERTY, injector);

		context.register(SimpleResponseWriter.class);
		context.register(DataResponseWriter.class);

		context.register(ResponseStatusDynamicFeature.class);

		for (Class<?> c : extraComponents) {
			context.register(c);
		}

		for (Feature f : extraFeatures) {
			f.configure(context);
		}

		return true;
	}

}
