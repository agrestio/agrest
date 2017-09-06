package com.nhl.link.rest.runtime;

import org.apache.cayenne.di.spi.ModuleProvider;

/**
 * Provider of extension modules for {@link LinkRestRuntime}. Used either directly or via {@link java.util.ServiceLoader}
 * API.
 *
 * @since 2.10
 */
public interface LrModuleProvider extends ModuleProvider {

}
