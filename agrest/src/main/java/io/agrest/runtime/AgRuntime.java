package io.agrest.runtime;

import io.agrest.provider.EntityUpdateCollectionReader;
import io.agrest.provider.EntityUpdateReader;
import io.agrest.provider.ResponseStatusDynamicFeature;
import io.agrest.runtime.provider.CayenneExpProvider;
import io.agrest.runtime.provider.IncludeProvider;
import io.agrest.runtime.provider.MapByProvider;
import io.agrest.runtime.provider.SizeProvider;
import io.agrest.runtime.provider.SortProvider;
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
 * Stores Agrest runtime stack packaged as a JAX RS {@link Feature}.
 */
public class AgRuntime implements Feature {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgRuntime.class);

    static final String AGREST_CONTAINER_PROPERTY = "agrest.container";

    public static final String BODY_WRITERS_MAP = "agrest.jaxrs.bodywriters";

    private Injector injector;
    private Collection<Feature> extraFeatures;

    /**
     * Returns a service of a specified type present in Agrest container that
     * is stored in JAX RS Configuration.
     */
    public static <T> T service(Class<T> type, Configuration config) {

        if (config == null) {
            throw new NullPointerException("Null config");
        }

        Injector injector = (Injector) config.getProperty(AGREST_CONTAINER_PROPERTY);
        if (injector == null) {
            throw new IllegalStateException(
                    "Agrest is misconfigured. No injector found for property: " + AGREST_CONTAINER_PROPERTY);
        }

        return injector.getInstance(type);
    }

    AgRuntime(Injector injector, Collection<Feature> extraFeatures) {
        this.injector = injector;
        this.extraFeatures = extraFeatures;
    }

    /**
     * Returns a Agrest service instance of a given type stored in the internal DI container.
     */
    public <T> T service(Class<T> type) {
        return injector.getInstance(type);
    }

    /**
     * Returns a Agrest service instance of a given type stored in the internal DI container.
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
        LOGGER.info("Shutting down Agrest");
        injector.shutdown();
    }

    @Override
    public boolean configure(FeatureContext context) {

        // this gives everyone access to the Agrest services
        context.property(AgRuntime.AGREST_CONTAINER_PROPERTY, injector);

        @SuppressWarnings("unchecked")
        Map<String, Class> bodyWriters =
                injector.getInstance(Key.getMapOf(String.class, Class.class, AgRuntime.BODY_WRITERS_MAP));

        for (Class<?> type : bodyWriters.values()) {
            context.register(type);
        }

        CayenneExpProvider cayenneExpProvider =
                injector.getInstance(CayenneExpProvider.class);
        context.register(cayenneExpProvider);

        IncludeProvider includeProvider =
                injector.getInstance(IncludeProvider.class);
        context.register(includeProvider);

        SortProvider sortProvider =
                injector.getInstance(SortProvider.class);
        context.register(sortProvider);

        MapByProvider mapByProvider =
                injector.getInstance(MapByProvider.class);
        context.register(mapByProvider);

        SizeProvider sizeProvider =
                injector.getInstance(SizeProvider.class);
        context.register(sizeProvider);

        context.register(ResponseStatusDynamicFeature.class);

        context.register(EntityUpdateReader.class);
        context.register(EntityUpdateCollectionReader.class);

        for (Feature f : extraFeatures) {
            f.configure(context);
        }

        return true;
    }
}
