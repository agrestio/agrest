package com.nhl.link.rest.runtime;

import com.nhl.link.rest.provider.EntityUpdateCollectionReader;
import com.nhl.link.rest.provider.EntityUpdateReader;
import com.nhl.link.rest.provider.ResponseStatusDynamicFeature;
import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Collection;
import java.util.Map;

/**
 * Stores LinkRest runtime stack packaged as a JAX RS {@link Feature}.
 */
public class LinkRestRuntime implements Feature {

    private static final Logger LOGGER = LoggerFactory.getLogger(LinkRestRuntime.class);

    static final String LINK_REST_CONTAINER_PROPERTY = "linkrest.container";

    public static final String BODY_WRITERS_MAP = "linkrest.jaxrs.bodywriters";

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
            throw new IllegalStateException(
                    "LinkRest is misconfigured. No injector found for property: " + LINK_REST_CONTAINER_PROPERTY);
        }

        return injector.getInstance(type);
    }

    LinkRestRuntime(Injector injector, Collection<Feature> extraFeatures, Collection<Class<?>> extraComponents) {
        this.injector = injector;
        this.extraFeatures = extraFeatures;
        this.extraComponents = extraComponents;
    }

    /**
     * Returns a LinkRest service instance of a given type stored in the internal DI container.
     */
    public <T> T service(Class<T> type) {
        return injector.getInstance(type);
    }

    /**
     * Returns a LinkRest service instance of a given type stored in the internal DI container.
     *
     * @since 2.10
     */
    public <T> T service(Key<T> key) {
        return injector.getInstance(key);
    }

    /**
     * @since 2.0
     */
    public void shutdown() {
        LOGGER.info("Shutting down LinkRest");
        injector.shutdown();
    }

    @Override
    public boolean configure(FeatureContext context) {

        // this gives everyone access to the LinkRest services
        context.property(LinkRestRuntime.LINK_REST_CONTAINER_PROPERTY, injector);

        @SuppressWarnings("unchecked")
        Map<String, Class> bodyWriters =
                injector.getInstance(Key.getMapOf(String.class, Class.class, LinkRestRuntime.BODY_WRITERS_MAP));

        for (Class<?> type : bodyWriters.values()) {
            context.register(type);
        }

        context.register(ResponseStatusDynamicFeature.class);

        context.register(EntityUpdateReader.class);
        context.register(EntityUpdateCollectionReader.class);

        for (Class<?> c : extraComponents) {
            context.register(c);
        }

        for (Feature f : extraFeatures) {
            f.configure(context);
        }

        return true;
    }
}
