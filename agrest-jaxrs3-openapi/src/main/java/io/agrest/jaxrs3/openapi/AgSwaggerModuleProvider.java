package io.agrest.jaxrs3.openapi;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

public class AgSwaggerModuleProvider implements AgModuleProvider {

    @Override
    public Module module() {
        return new AgSwaggerModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return AgSwaggerModule.class;
    }
}
