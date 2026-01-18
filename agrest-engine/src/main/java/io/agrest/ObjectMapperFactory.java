package io.agrest;

import io.agrest.runtime.processor.update.ByIdObjectMapperFactory;
import io.agrest.runtime.processor.update.ByPropertiesObjectMapperFactory;
import io.agrest.runtime.processor.update.ByPropertyObjectMapperFactory;
import io.agrest.runtime.processor.update.UpdateContext;

/**
 * A factory of a strategy for mapping update operations to existing objects.
 *
 * @since 1.7
 */
public interface ObjectMapperFactory {

    /**
     * @since 5.0
     */
    static ObjectMapperFactory matchById() {
        return ByIdObjectMapperFactory.mapper();
    }

    /**
     * @since 5.0
     */
    static ObjectMapperFactory matchByProperties(String... properties) {
        return switch (properties.length) {
            case 0 -> throw new IllegalArgumentException("No properties specified for ObjectMapperFactory");
            case 1 -> new ByPropertyObjectMapperFactory(properties[0]);
            default -> new ByPropertiesObjectMapperFactory(properties);
        };
    }

    /**
     * Returns a mapper to handle objects of a given response.
     */
    <T> ObjectMapper<T> createMapper(UpdateContext<T> context);
}