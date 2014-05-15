package com.nhl.link.rest.runtime;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.di.Injector;

import com.nhl.link.rest.provider.CayenneRuntimeExceptionMapper;
import com.nhl.link.rest.provider.DataResponseWriter;
import com.nhl.link.rest.provider.LinkRestExceptionMapper;
import com.nhl.link.rest.provider.SimpleResponseWriter;

/**
 * A JAX RS "feature" that bootstraps LinkRest in an app.
 */
class LinkRestFeature implements Feature {

	private Injector injector;

	LinkRestFeature(Injector injector) {
		this.injector = injector;
	}

	@Override
	public boolean configure(FeatureContext context) {

		// this gives everyone access to the LinkRest services
		context.property(LinkRestRuntime.LINK_REST_CONTAINER_PROPERTY, injector);

		context.register(SimpleResponseWriter.class);
		context.register(DataResponseWriter.class);
		context.register(CayenneRuntimeExceptionMapper.class);
		context.register(LinkRestExceptionMapper.class);

		return true;
	}

}
