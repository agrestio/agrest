package io.agrest.cayenne;

import io.agrest.cayenne.persister.CayennePersister;
import io.agrest.cayenne.persister.ICayennePersister;
import org.apache.cayenne.configuration.server.ServerRuntime;

/**
 * Cayenne extension builder for Agrest. Must be initialized with an externally created Cayenne ServerRuntime.
 *
 * @since 3.4
 */
public class AgCayenneBuilder {

    private ICayennePersister persister;

    /**
     * A shortcut that creates a Agrest Cayenne extension based on Cayenne runtime and default settings.
     */
    public static AgCayenneModule build(ServerRuntime cayenneRuntime) {
        return builder(cayenneRuntime).build();
    }

    /**
     * A shortcut that creates an AgCayenneBuilder, setting its Cayenne runtime. The caller can continue customizing
     * the returned builder.
     */
    public static AgCayenneBuilder builder() {
        return new AgCayenneBuilder();
    }

    public static AgCayenneBuilder builder(ServerRuntime cayenneRuntime) {
        return new AgCayenneBuilder().runtime(cayenneRuntime);
    }

    protected AgCayenneBuilder() {
    }

    public AgCayenneBuilder runtime(ServerRuntime cayenneRuntime) {
        this.persister = new CayennePersister(cayenneRuntime);
        return this;
    }

    public AgCayenneBuilder persister(ICayennePersister persister) {
        this.persister = persister;
        return this;
    }

    public AgCayenneModule build() {
        return new AgCayenneModule(persister);
    }
}
