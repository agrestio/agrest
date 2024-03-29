package io.agrest.converter.jsonvalue;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.reflect.PropertySetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PojoConverter<T> extends AbstractConverter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PojoConverter.class);

    private final Class<T> type;

    private final Map<String, PropertySetter> setters;
    private final Map<String, JsonValueConverter<?>> propertyConverters;
    private final JsonValueConverter<?> defaultConverter;

    // TODO: get rid of two maps (combine setter and converter in some interface, e.g. PropertyWriter)
    public PojoConverter(
            Class<T> type,
            Map<String, PropertySetter> setters,
            Map<String, JsonValueConverter<?>> propertyConverters,
            JsonValueConverter defaultConverter) {
        this.type = type;
        this.setters = setters;
        this.propertyConverters = propertyConverters;
        this.defaultConverter = defaultConverter;
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate type: " + type.getName(), e);
        }
    }

    @Override
    public T valueNonNull(JsonNode node) {
        T object = newInstance(type);

        node.fields().forEachRemaining(e -> {
            String propertyName = e.getKey();
            JsonNode value = e.getValue();

            if (value.isNull() || value.isMissingNode()) {
                LOGGER.debug("Skipping null/missing node: '{}'", propertyName);
                return;
            }

            PropertySetter setter = setters.get(propertyName);
            if (setter == null) {
                LOGGER.debug("Skipping unknown property: '{}'", propertyName);
                return;
            }

            JsonValueConverter<?> converter = getPropertyConverter(propertyName);
            Object convertedValue = converter.value(value);
            if (convertedValue != null) {
                setter.setValue(object, convertedValue);
            }
        });

        return object;
    }

    private JsonValueConverter<?> getPropertyConverter(String propertyName) {
        return propertyConverters.getOrDefault(propertyName, defaultConverter);
    }
}
