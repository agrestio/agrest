package io.agrest.jaxrs.openapi;

import io.agrest.jaxrs2.AgFeatureProvider;
import io.agrest.jaxrs.openapi.modelconverter.AgEntityModelConverter;
import io.agrest.jaxrs.openapi.modelconverter.AgProtocolModelConverter;
import io.agrest.jaxrs.openapi.modelconverter.AgValueModelConverter;
import io.agrest.runtime.AgRuntime;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Feature;

/**
 * Installs {@link AgEntityModelConverter} in the Swagger runtime via the {@link AgFeatureProvider} mechanism.
 */
public class AgSwaggerModuleInstaller implements AgFeatureProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgSwaggerModuleInstaller.class);

    @Override
    public Feature feature(AgRuntime runtime) {

        LOGGER.info("initializing Agrest Swagger model converters");

        installConverter(runtime.service(AgValueModelConverter.class));
        installConverter(runtime.service(AgProtocolModelConverter.class));
        installConverter(runtime.service(AgEntityModelConverter.class));

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
