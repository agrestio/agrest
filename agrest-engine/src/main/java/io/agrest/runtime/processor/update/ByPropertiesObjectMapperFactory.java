package io.agrest.runtime.processor.update;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

/**
 * @since 5.0
 */
public class ByPropertiesObjectMapperFactory implements ObjectMapperFactory {

    private final String[] properties;

    public ByPropertiesObjectMapperFactory(String... properties) {
        this.properties = properties;
    }

    @Override
    public <T> ObjectMapper<T> createMapper(UpdateContext<T> context) {
        AgEntity<T> entity = context.getEntity().getAgEntity();

        int len = properties.length;
        AgAttribute[] attributes = new AgAttribute[len];
        for (int i = 0; i < len; i++) {
            // TODO: should we account for "id" attributes here?
            attributes[i] = entity.getAttribute(properties[i]);
        }

        return new ByPropertiesObjectMapper<>(attributes);
    }
}
