package io.agrest.runtime.processor.update;

import io.agrest.ObjectMapper;
import io.agrest.ObjectMapperFactory;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

/**
 * @since 5.0
 */
public class ByPropertyObjectMapperFactory implements ObjectMapperFactory {

    private final String property;

    public ByPropertyObjectMapperFactory(String property) {
        this.property = property;
    }

    @Override
    public <T> ObjectMapper<T> createMapper(UpdateContext<T> context) {
        AgEntity<T> entity = context.getEntity().getAgEntity();

        // TODO: should we account for "id" attributes here?
        AgAttribute attribute = entity.getAttribute(property);
        
        return new ByPropertyObjectMapper<>(attribute);
    }
}
