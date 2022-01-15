package io.agrest.converter.valuestring;

import io.agrest.AgException;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 5.0
 */
public class ValueStringConverterFactory implements IValueStringConverterFactory {

    private final Map<Class<?>, ValueStringConverter> convertersByJavaType;
    private final ValueStringConverter defaultConverter;

    // these are explicit overrides for named attributes
    private final Map<String, ValueStringConverter> convertersByPath;

    public ValueStringConverterFactory(
            Map<Class<?>, ValueStringConverter> knownConverters,
            ValueStringConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
        this.convertersByPath = new ConcurrentHashMap<>();
    }

    @Override
    public Map<Class<?>, ValueStringConverter> getConverters() {
        return convertersByJavaType;
    }

    @Override
    public ValueStringConverter getConverter(Class<?> type) {
        return convertersByJavaType.getOrDefault(type, defaultConverter);
    }

    @Override
    public ValueStringConverter getConverter(AgEntity<?> entity) {
        return getConverter(entity, null);
    }

    @Override
    public ValueStringConverter getConverter(AgEntity<?> entity, String attributeName) {
        String key = attributeName != null ? entity.getName() + "." + attributeName : entity.getName();
        return convertersByPath.computeIfAbsent(key, k -> buildConverter(entity, attributeName));
    }

    protected ValueStringConverter buildConverter(AgEntity<?> entity, String attributeName) {

        if (attributeName == null) {
            // root object encoder... assuming we'll get ID as number
            return GenericConverter.converter();
        }

        AgAttribute attribute = entity.getAttribute(attributeName);

        if (attribute == null) {
            throw AgException.badRequest("Invalid attribute: '%s.%s'",  entity.getName(), attributeName);
        }

        return buildConverter(attribute);
    }

    protected ValueStringConverter buildConverter(AgAttribute attribute) {
        return getConverter(attribute.getType());
    }
}
