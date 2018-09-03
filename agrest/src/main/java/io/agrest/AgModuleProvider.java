package io.agrest;

import io.agrest.runtime.LinkRestRuntime;
import org.apache.cayenne.di.Module;
import org.apache.cayenne.di.spi.ModuleProvider;

import java.util.Collection;
import java.util.Collections;

/**
 * Provider of extension modules for {@link LinkRestRuntime}. Used either directly or via {@link java.util.ServiceLoader}
 * API.
 *
 * @since 2.10
 */
public interface AgModuleProvider extends ModuleProvider {

    @Override
    default Collection<Class<? extends Module>> overrides() {
        return Collections.emptySet();
    }
}
