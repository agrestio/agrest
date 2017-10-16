package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.apache.cayenne.DataObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CayenneJsonEntityReader<T extends DataObject> implements IJsonEntityReader<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CayenneJsonEntityReader.class);

    private final Class<T> persistentType;
    private final IJsonValueConverterFactory converterFactory;
    private final Map<String, JsonValueConverter> propertyConverters;
    private final PropertyHelper propertyHelper;

    public CayenneJsonEntityReader(Class<T> persistentType, IJsonValueConverterFactory converterFactory) {
        this.persistentType = persistentType;
        this.converterFactory = converterFactory;

        int estimatedSize = (int) ((persistentType.getMethods().length / 0.75d) + 1);
        this.propertyConverters = new ConcurrentHashMap<>(estimatedSize);
        this.propertyHelper = new PropertyHelper(persistentType);
    }

    @Override
    public T readEntity(JsonNode node) {
        T object = newInstance(persistentType);

        node.fields().forEachRemaining(e -> {
            String propertyName = e.getKey();
            JsonNode value = e.getValue();

            if (value.isNull() || value.isMissingNode()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Skipping null/missing node: '{}'", propertyName);
                }
                return;
            }

            if (value.isArray() || value.isObject() || value.isPojo()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Skipping array/object/pojo node: '{}'", propertyName);
                }
                return;
            }

            // TODO: use proper JsonValueConverter based on getter method's return type
            // TODO: skip Json attributes, for which there does not exist a getter method
            JsonValueConverter converter = getPropertyConverter(propertyName);
            object.writePropertyDirectly(propertyName, converter.value(value));
        });

        return object;
    }

    private JsonValueConverter getPropertyConverter(String propertyName) {
        Class<?> propertyType = propertyHelper.getPropertyType(propertyName).orElse(Object.class);
        return propertyConverters.computeIfAbsent(propertyName, s -> converterFactory.converter(propertyType));
    }

    private static class PropertyHelper {

        private final Map<String, Class<?>> methodReturnTypes;

        PropertyHelper(Class<?> persistentType) {
            this.methodReturnTypes = collectMethodReturnTypes(persistentType);
        }

        private Map<String, Class<?>> collectMethodReturnTypes(Class<?> type) {
            Map<String, Class<?>> methodReturnTypes = new ConcurrentHashMap<>();
            // TODO: check if:
            // - name starts with get,
            // - has overloaded versions
            // - overrides returns types of inherited methods
            // => need to traverse DFS-style
            for (Method method : type.getMethods()) {
                methodReturnTypes.put(method.getName(), method.getReturnType());
            }
            return methodReturnTypes;
        }

        Optional<Class<?>> getPropertyType(String propertyName) {
            return Optional.ofNullable(methodReturnTypes.get(toGetterName(propertyName)));
        }

        private static String toGetterName(String propertyName) {
            if (propertyName.isEmpty()) {
                throw new IllegalArgumentException("Empty property name");
            }

            String getterName = "get" + propertyName.substring(0, 1).toUpperCase();
            if (propertyName.length() > 1) {
                getterName += propertyName.substring(1);
            }
            return getterName;
        }
    }

    private static <T> T newInstance(Class<T> type) {
        try {
            return type.newInstance();
        } catch (Exception e) {
            throw new LinkRestClientException("Failed to instantiate type: " + type.getName(), e);
        }
    }
}
