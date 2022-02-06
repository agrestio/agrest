package io.agrest.jaxrs3;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 * @deprecated since 5.0. Exists to load a few remaining deprecated core services that have JAX-RS dependencies. As those
 * services go away, this provider will be removed as well,
 */
@Deprecated
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
