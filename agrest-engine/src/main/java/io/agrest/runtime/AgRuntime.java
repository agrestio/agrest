package io.agrest.runtime;

import org.apache.cayenne.di.Injector;
import org.apache.cayenne.di.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents Agrest stack.
 */
public class AgRuntime {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgRuntime.class);

    private final Injector injector;

    AgRuntime(Injector injector) {
        this.injector = injector;
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
}
