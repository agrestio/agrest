package io.agrest.multisource;

import io.agrest.SelectBuilder;
import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Configuration;
import java.util.concurrent.ExecutorService;

/**
 * A builder of multi-source request chains.
 *
 * @see io.agrest.meta.AgEntityOverlay
 * @see io.agrest.resolver.NestedDataResolver
 * @since 2.0
 * @deprecated since 3.4 as we now have per-entity data resolvers, so data from multiple sources can be resolved within
 * a regular pipeline, and without the need to add fake properties to the root objects to store nested results.
 */
@Deprecated
public class AgMultiSource {

    /**
     * Starts a {@link MultiSelectBuilder} that allows to fetch data from
     * multiple sources in parallel with minimal or no blocking.
     *
     * @param rootSelectChain A root Agrest select chain. Any other results will be
     *                        attached to the response from the root chain.
     * @param config          JAX RS configuration that allows the method to locate Agrest
     *                        stack.
     * @return MultiSelectBuilder instance.
     */
    public static <T> MultiSelectBuilder<T> select(SelectBuilder<T> rootSelectChain, Configuration config) {

        ExecutorService executor = AgRuntime.service(ExecutorService.class, config);
        return new MultiSelectBuilder<>(rootSelectChain, executor);
    }
}
