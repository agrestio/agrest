package io.agrest.jaxrs3.openapi;

import io.agrest.jaxrs3.openapi.modelconverter.AgEntityModelConverter;
import io.agrest.jaxrs3.openapi.modelconverter.AgProtocolModelConverter;
import io.agrest.jaxrs3.openapi.modelconverter.AgValueModelConverter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

/**
 * @since 5.0
 */
public class AgSwaggerDefaultsModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bindList(String.class, AgEntityModelConverter.BINDING_ENTITY_PACKAGES);
        binder.bind(AgValueModelConverter.class).toInstance(AgValueModelConverter.getInstance());
        binder.bind(AgProtocolModelConverter.class).to(AgProtocolModelConverter.class);
        binder.bind(AgEntityModelConverter.class).to(AgEntityModelConverter.class);
    }
}
