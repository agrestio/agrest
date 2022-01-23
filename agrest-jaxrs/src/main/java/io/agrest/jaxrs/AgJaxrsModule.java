package io.agrest.jaxrs;

import io.agrest.jaxrs.meta.parser.ResourceParser;
import io.agrest.meta.parser.IResourceParser;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 * @deprecated since 5.0 loads some deprecated service with JAX-RS dependency
 */
@Deprecated
public class AgJaxrsModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind(IResourceParser.class).to(ResourceParser.class);
    }
}
