package io.agrest.jaxrs3;

import io.agrest.jaxrs3.meta.parser.ResourceParser;
import io.agrest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 * @deprecated since 5.0. Exists to load a few remaining deprecated core services that have JAX-RS dependencies. As those
 * services go away, this module will be removed as well,
 */
@Deprecated
public class AgJaxrsModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(IResourceParser.class).to(ResourceParser.class);
    }
}
