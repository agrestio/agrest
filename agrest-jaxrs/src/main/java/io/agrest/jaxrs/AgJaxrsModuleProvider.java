package io.agrest.jaxrs;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 * @deprecated since 5.0 loads some deprecated service with JAX-RS dependency
 */
public class AgJaxrsModuleProvider implements AgModuleProvider {

    @Override
    public Module module() {
        return new AgJaxrsModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return AgJaxrsModule.class;
    }
}
