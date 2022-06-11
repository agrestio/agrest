package io.agrest.jaxrs3.openapi;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 */
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
