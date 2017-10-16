package com.nhl.link.rest.client.runtime.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.nhl.link.rest.client.LinkRestClientException;
import com.nhl.link.rest.meta.compiler.BeanAnalyzer;
import com.nhl.link.rest.meta.compiler.PropertyGetter;
import com.nhl.link.rest.meta.compiler.PropertySetter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.runtime.parser.converter.IJsonValueConverterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PojoJsonEntityReader<T> implements IJsonEntityReader<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(PojoJsonEntityReader.class);

    private final Class<T> entityType;
    private final IJsonValueConverterFactory converterFactory;
    private final Map<String, JsonValueConverter> propertyConverters;
    private final PropertyHelper propertyHelper;

    public PojoJsonEntityReader(Class<T> entityType, IJsonValueConverterFactory converterFactory) {
        this.entityType = entityType;
        this.converterFactory = converterFactory;

        int estimatedSize = (int) ((entityType.getMethods().length / 0.75d) + 1);
        this.propertyConverters = new ConcurrentHashMap<>(estimatedSize);
        this.propertyHelper = new PropertyHelper(entityType);
    }

    @Override
    public T readEntity(JsonNode node) {
        T object = newInstance(entityType);

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

            JsonValueConverter converter = getPropertyConverter(propertyName);
            Object convertedValue = converter.value(value);
            if (convertedValue != null) {
                propertyHelper.getPropertySetter(propertyName, convertedValue.getClass())
                        .ifPresent(setter -> setter.setValue(object, convertedValue));
            }
        });

        return object;
    }

    private JsonValueConverter getPropertyConverter(String propertyName) {
        Class<?> propertyType = propertyHelper.getPropertyGetter(propertyName)
                .map(PropertyGetter::getType).orElse(Object.class);
        return propertyConverters.computeIfAbsent(propertyName, s -> converterFactory.converter(propertyType));
    }

    private static class PropertyHelper {

        private final Map<String, PropertyGetter> getters;
        private final Map<String, Map<Class<?>, PropertySetter>> setters;

        PropertyHelper(Class<?> type) {
            this.getters = BeanAnalyzer.findGetters(type)
                    .collect(Collectors.toMap(getter -> getter.getMethod().getName(), Function.identity()));

            Map<String, Map<Class<?>, PropertySetter>> setters = new HashMap<>();
            BeanAnalyzer.findSetters(type).forEach(setter -> {
                String methodName = setter.getMethod().getName();
                Map<Class<?>, PropertySetter> overloadedSetters = setters.get(methodName);
                if (overloadedSetters == null) {
                    overloadedSetters = new HashMap<>();
                    setters.put(methodName, overloadedSetters);
                }
                overloadedSetters.put(setter.getType(), setter);
            });
            this.setters = setters;
        }

        Optional<PropertyGetter> getPropertyGetter(String propertyName) {
            return toGetterNames(propertyName).stream()
                    .map(getters::get)
                    .filter(it -> it != null)
                    .findFirst();
        }

        private static Collection<String> toGetterNames(String propertyName) {
            if (propertyName.isEmpty()) {
                throw new IllegalArgumentException("Empty property name");
            }

            String capitalized = toCapitalized(propertyName);
            String getterName = "get" + capitalized;
            String booleanGetterName = "is" + capitalized;

            return Arrays.asList(getterName, booleanGetterName);
        }

        Optional<PropertySetter> getPropertySetter(String propertyName, Class<?> valueType) {
            PropertySetter setter = setters.getOrDefault(toSetterName(propertyName), Collections.emptyMap())
                    .get(valueType);
            return Optional.ofNullable(setter);
        }

        private static String toSetterName(String propertyName) {
            if (propertyName.isEmpty()) {
                throw new IllegalArgumentException("Empty property name");
            }
            return "set" + toCapitalized(propertyName);
        }

        private static String toCapitalized(String s) {
            return Character.toUpperCase(s.charAt(0)) + s.substring(1);
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
