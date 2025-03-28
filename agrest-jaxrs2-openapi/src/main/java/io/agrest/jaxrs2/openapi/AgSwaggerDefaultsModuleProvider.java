package io.agrest.jaxrs2.openapi;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public class AgSwaggerDefaultsModuleProvider implements AgModuleProvider {

    @Override
    public Module module() {
        return new AgSwaggerDefaultsModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return AgSwaggerDefaultsModule.class;
    }
}
