package io.agrest.runtime.cayenne;

import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.IAgPersister;
import org.apache.cayenne.configuration.server.ServerRuntime;

/**
 *
 * A builder of Agrest runtime that wraps io.agrest.runtime.AgCayenneBuilder based on the Cayenne runtime
 */
public class AgCayenneBuilder {

    /**
     * A shortcut that creates a Agrest stack based on Cayenne runtime and
     * default settings.
     *
     * @since 1.14
     */
    public static AgRuntime build(ServerRuntime cayenneRuntime) {
        return builder(cayenneRuntime).build();
    }

    /**
     * A shortcut that creates a AgCayenneBuilder, setting its Cayenne runtime. A
     * caller can continue customizing the returned builder.
     *
     * @since 1.14
     */
    public static AgBuilder builder(ServerRuntime cayenneRuntime) {

        CayennePersister cayennePersister = new CayennePersister(cayenneRuntime);

        return new AgBuilder()
                .agPersister(cayennePersister)
                .module(binder -> {
                    binder.bind(IAgPersister.class).toInstance(cayennePersister);
                });
    }
}
