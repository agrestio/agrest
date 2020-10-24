package io.agrest.openapi;

import io.agrest.AgFeatureProvider;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import org.apache.cayenne.di.Injector;

import javax.ws.rs.core.Feature;

/**
 * Installs {@link AgEntityModelConverter} in the Swagger runtime via the {@link AgFeatureProvider} mechanism.
 */
public class AgSwaggerModuleInstaller implements AgFeatureProvider {

    @Override
    public Feature feature(Injector injector) {

        installConverter(injector.getInstance(AgProtocolModelConverter.class));
        installConverter(injector.getInstance(AgEntityModelConverter.class));

        // Return a dummy feature as we are using this method for its side effects for its Injector access
        return fc -> false;
    }

    private void installConverter(ModelConverter converter) {
        // since ModelConverters is a static singleton, let's make sure we are not registering the same converter twice
        if (!ModelConverters.getInstance().getConverters().contains(converter)) {
            ModelConverters.getInstance().addConverter(converter);
        }
    }
}
