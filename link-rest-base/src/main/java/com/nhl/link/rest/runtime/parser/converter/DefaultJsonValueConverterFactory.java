package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.ThrowingSupplier;
import com.nhl.link.rest.meta.Types;
import com.nhl.link.rest.meta.compiler.BeanAnalyzer;
import com.nhl.link.rest.meta.compiler.PropertySetter;
import com.nhl.link.rest.parser.converter.CollectionConverter;
import com.nhl.link.rest.parser.converter.DelegatingConverter;
import com.nhl.link.rest.parser.converter.EnumConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.PojoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @since 1.10
 */
public class DefaultJsonValueConverterFactory implements IJsonValueConverterFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultJsonValueConverterFactory.class);

    protected Map<Type, JsonValueConverter<?>> convertersByJavaType;

    private JsonValueConverter<?> defaultConverter;

    public DefaultJsonValueConverterFactory(
            Map<Class<?>, JsonValueConverter<?>> knownConverters,
            JsonValueConverter<?> defaultConverter) {

        this.defaultConverter = defaultConverter;

        // creating a concurrent copy of the provided map - we'll be expanding it dynamically.
        this.convertersByJavaType = new ConcurrentHashMap<>(knownConverters);
    }

    @Override
    public JsonValueConverter<?> converter(Type valueType) {
        JsonValueConverter<?> converter = convertersByJavaType.get(valueType);
        if (converter == null) {
            ThrowingSupplier<JsonValueConverter<?>> converterSupplier = new ThrowingSupplier<>();
            converter = new DelegatingConverter(converterSupplier);
            JsonValueConverter<?> existing = convertersByJavaType.putIfAbsent(valueType, converter);
            if (existing != null) {
                converter = existing;
            } else {
                converterSupplier.setValue(buildConverter(valueType));
            }
        }
        return converter;
    }

    private JsonValueConverter<?> buildConverter(Type t) {
        Optional<JsonValueConverter<?>> converter = Types.getClassForType(t).map(cls -> buildConverter(cls, t));
        if (converter.isPresent()) {
            return converter.get();
        } else {
            return defaultConverter;
        }
    }

    @SuppressWarnings("unchecked")
    private JsonValueConverter<?> buildConverter(Class<?> cls, Type t) {
        if (cls.isEnum()) {
            return enumConverter((Class<? extends Enum<?>>)cls);
        }

        if (Collection.class.isAssignableFrom(cls)) {
            Type parameterType = Types.unwrapTypeArgument(t).orElse(Object.class);
            Supplier<Collection<Object>> containerSupplier;
            if (List.class.equals(cls) || Collection.class.equals(cls)) {
                containerSupplier = ArrayList::new;
            } else if (Set.class.equals(cls)) {
                containerSupplier = HashSet::new;
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unsupported collection type: " + cls.getName());
                }
                return defaultConverter;
            }
            JsonValueConverter<Object> elementConverter = (JsonValueConverter<Object>) converter(parameterType);
            return new CollectionConverter<>(containerSupplier, elementConverter);
        }

        return buildPojoConverter(cls);
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> JsonValueConverter<?> enumConverter(Class<? extends Enum<?>> enumType) {
        return new EnumConverter<>((Class<T>) enumType);
    }

    private JsonValueConverter<?> buildPojoConverter(Class<?> cls) {
        Map<String, PropertySetter> setters = BeanAnalyzer.findSetters(cls)
                .collect(Collectors.toMap(PropertySetter::getName, Function.identity()));

        if (setters.isEmpty()) {
            return defaultConverter;

        } else {
            Map<String, JsonValueConverter<?>> propertyConverters = setters.values().stream()
                .collect(Collectors.toMap(PropertySetter::getName, this::buildConverter));
            return new PojoConverter<>(cls, setters, propertyConverters, defaultConverter);
        }
    }

    private JsonValueConverter buildConverter(PropertySetter setter) {
        return converter(setter.getMethod().getGenericParameterTypes()[0]);
    }
}
