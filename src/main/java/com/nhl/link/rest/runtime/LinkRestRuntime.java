package com.nhl.link.rest.runtime;

import java.util.Collection;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import org.apache.cayenne.di.Injector;

import com.nhl.link.rest.provider.DataResponseWriter;
import com.nhl.link.rest.provider.ResponseStatusDynamicFeature;
import com.nhl.link.rest.provider.SimpleResponseWriter;
import com.nhl.link.rest.provider.UpdateResponseWriter;

/**
 * Stores LinkRest runtime stack packaged as a JAX RS {@link Feature}.
 */
public class LinkRestRuntime implements Feature {

	static final String LINK_REST_CONTAINER_PROPERTY = "linkrest.container";

	private Injector injector;
	private Collection<Class<?>> extraComponents;
	private Collection<Feature> extraFeatures;

	/**
	 * Returns a service of a specified type present in LinkRest container that
	 * is stored in JAX RS Configuration.
	 */
	public static <T> T service(Class<T> type, Configuration config) {

		if (config == null) {
			throw new NullPointerException("Null config");
		}

		Injector injector = (Injector) config.getProperty(LINK_REST_CONTAINER_PROPERTY);
		if (injector == null) {
			throw new IllegalStateException("LinkRest is misconfigured. No injector found for property: "
					+ LINK_REST_CONTAINER_PROPERTY);
		}

		return injector.getInstance(type);
	}

	LinkRestRuntime(Injector injector, Collection<Feature> extraFeatures, Collection<Class<?>> extraComponents) {
		this.injector = injector;
		this.extraFeatures = extraFeatures;
		this.extraComponents = extraComponents;
	}

	/**
	 * @deprecated since 1.14, as LinkRestRuntime implements Feature itself.
	 */
	@Deprecated
	public Feature getFeature() {
		return this;
	}

	/**
	 * Returns a LinkRest service instance of a given type stored in the
	 * internal DI container.
	 */
	public <T> T service(Class<T> type) {
		return injector.getInstance(type);
	}

	@Override
	public boolean configure(FeatureContext context) {

		// this gives everyone access to the LinkRest services
		context.property(LinkRestRuntime.LINK_REST_CONTAINER_PROPERTY, injector);

		context.register(SimpleResponseWriter.class);
		context.register(DataResponseWriter.class);
		context.register(UpdateResponseWriter.class);

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
