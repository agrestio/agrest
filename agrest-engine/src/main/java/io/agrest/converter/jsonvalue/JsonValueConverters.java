package io.agrest.converter.jsonvalue;

import io.agrest.reflect.BeanAnalyzer;
import io.agrest.reflect.PropertySetter;
import io.agrest.reflect.Types;
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
 *  Provides access to encoders of simple values for different Java types. Can be preconfigured to use custom encoders
 *  via DI.
 *
 * @since 5.0
 */
public class JsonValueConverters {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonValueConverters.class);

    protected final Map<Type, JsonValueConverter<?>> convertersByJavaType;
    private final JsonValueConverter<?> defaultConverter;

    public JsonValueConverters(
            Map<Class<?>, JsonValueConverter<?>> knownConverters,
            JsonValueConverter<?> defaultConverter) {

        this.defaultConverter = defaultConverter;

        // creating a concurrent copy of the provided map - we'll be expanding it dynamically.
        this.convertersByJavaType = new ConcurrentHashMap<>(knownConverters);
    }

    public JsonValueConverter<?> converter(Type valueType) {
        return convertersByJavaType.computeIfAbsent(valueType, this::buildOrDefault);
    }

    @SuppressWarnings("unchecked")
    public <T> JsonValueConverter<T> typedConverter(Class<T> valueType) {
        return (JsonValueConverter<T>) convertersByJavaType.computeIfAbsent(valueType, this::buildOrDefault);
    }

    private JsonValueConverter<?> buildOrDefault(Type t) {
        return Types.getClassForType(t).flatMap(cls -> buildConverter(cls, t)).orElse(defaultConverter);
    }

    private Optional<JsonValueConverter<?>> buildConverter(Class<?> cls, Type t) {
        if (cls.isEnum()) {
            return Optional.of(enumConverter(cls));
        }

        if (Collection.class.isAssignableFrom(cls)) {
            Class<?> parameterType = t != null ? Types.getClassForTypeArgument(t).orElse(Object.class) : Object.class;
            return Optional.ofNullable(collectionConverter(cls, parameterType));
        }

        return Optional.ofNullable(objectConverter(cls));
    }

    @SuppressWarnings("unchecked")
    private <T extends Enum<T>> JsonValueConverter<T> enumConverter(Class<?> enumType) {
        return new EnumConverter<>((Class<T>) enumType);
    }

    @SuppressWarnings("unchecked")
    private <T extends Collection<E>, E> JsonValueConverter<T> collectionConverter(Class<?> containerType, Class<E> elementType) {
        Supplier<T> containerSupplier;
        if (List.class.equals(containerType) || Collection.class.equals(containerType)) {
            containerSupplier = () -> (T) new ArrayList<>();
        } else if (Set.class.equals(containerType)) {
            containerSupplier = () -> (T) new HashSet<>();
        } else {
            LOGGER.debug("Unsupported collection type: {}", containerType.getName());
            return null;
        }
        JsonValueConverter<E> elementConverter = new LazyConverter<>(() -> typedConverter(elementType));
        return new CollectionConverter<>(containerSupplier, elementConverter);
    }

    private JsonValueConverter<?> objectConverter(Class<?> cls) {
        Map<String, PropertySetter> setters = BeanAnalyzer.findSetters(cls)
                .collect(Collectors.toMap(PropertySetter::getName, Function.identity()));

        if (setters.isEmpty()) {
            return null;

        } else {
            Map<String, JsonValueConverter<?>> propertyConverters = setters.values().stream()
                    .collect(Collectors.toMap(PropertySetter::getName, setter -> new LazyConverter<>(() -> buildConverter(setter))));
            return new PojoConverter<>(cls, setters, propertyConverters, defaultConverter);
        }
    }

    private JsonValueConverter<?> buildConverter(PropertySetter setter) {
        return converter(setter.getMethod().getGenericParameterTypes()[0]);
    }
}
