package io.agrest.runtime.encoder;

import io.agrest.AgException;
import io.agrest.converter.valuejson.GenericConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 5.0
 */
public class ValueJsonConverterFactory implements IValueJsonConverterFactory {

    private final Map<Class<?>, ValueJsonConverter> convertersByJavaType;
    private final ValueJsonConverter defaultConverter;

    // these are explicit overrides for named attributes
    private final Map<String, ValueJsonConverter> convertersByPath;

    public ValueJsonConverterFactory(
            Map<Class<?>, ValueJsonConverter> knownConverters,
            ValueJsonConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
        this.convertersByPath = new ConcurrentHashMap<>();
    }

    @Override
    public Map<Class<?>, ValueJsonConverter> getConverters() {
        return convertersByJavaType;
    }

    @Override
    public ValueJsonConverter getConverter(Class<?> type) {
        return convertersByJavaType.getOrDefault(type, defaultConverter);
    }

    @Override
    public ValueJsonConverter getConverter(AgEntity<?> entity) {
        return getConverter(entity, null);
    }

    @Override
    public ValueJsonConverter getConverter(AgEntity<?> entity, String attributeName) {
        String key = attributeName != null ? entity.getName() + "." + attributeName : entity.getName();
        return convertersByPath.computeIfAbsent(key, k -> buildConverter(entity, attributeName));
    }

    protected ValueJsonConverter buildConverter(AgEntity<?> entity, String attributeName) {

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

    protected ValueJsonConverter buildConverter(AgAttribute attribute) {
        return getConverter(attribute.getType());
    }
}
