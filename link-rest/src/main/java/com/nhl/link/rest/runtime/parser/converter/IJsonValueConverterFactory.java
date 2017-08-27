package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.parser.converter.JsonValueConverter;

/**
 * A service that ensures proper conversion of incoming JSON values to the
 * model-compatible Java types.
 *
 * @since 1.10
 */
public interface IJsonValueConverterFactory {

    /**
     * @since 1.24
     */
    JsonValueConverter converter(Class<?> valueType);

    /**
     * @since 2.5
     * @deprecated since 2.10 unused.
     */
    @Deprecated
    default JsonValueConverter converter(LrAttribute attribute) {
        return converter(attribute.getType());
    }

    /**
     * Get converter for entity ID.
     *
     * @throws IllegalArgumentException if entity has multiple IDs
     * @since 2.5
     * @deprecated since 2.10 unused.
     */
    @Deprecated
    default JsonValueConverter converter(LrEntity<?> entity) {
        int ids = entity.getIds().size();
        if (ids != 1) {
            throw new IllegalArgumentException("Entity '" + entity.getName() +
                    "' has unexpected number of ID attributes: " + ids);
        }
        return converter(entity.getIds().iterator().next().getType());
    }
}
