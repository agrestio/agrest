package io.agrest.jaxrs2.openapi;

import io.agrest.jaxrs2.openapi.modelconverter.AgEntityModelConverter;
import io.agrest.jaxrs2.openapi.modelconverter.AgProtocolModelConverter;
import io.agrest.jaxrs2.openapi.modelconverter.AgValueModelConverter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.ListBuilder;
import org.apache.cayenne.di.Module;

public class AgSwaggerModule implements Module {

    public static ListBuilder<String> contributeEntityPackages(Binder binder) {
        return binder.bindList(String.class, AgEntityModelConverter.BINDING_ENTITY_PACKAGES);
    }

    @Override
    public void configure(Binder binder) {
        binder.bindList(String.class, AgEntityModelConverter.BINDING_ENTITY_PACKAGES);
        binder.bind(AgValueModelConverter.class).toInstance(AgValueModelConverter.getInstance());
        binder.bind(AgProtocolModelConverter.class).to(AgProtocolModelConverter.class);
        binder.bind(AgEntityModelConverter.class).to(AgEntityModelConverter.class);
    }
}
