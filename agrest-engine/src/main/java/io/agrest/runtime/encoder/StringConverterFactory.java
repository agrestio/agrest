package io.agrest.runtime.encoder;

import io.agrest.AgException;
import io.agrest.converter.valuejson.GenericConverter;
import io.agrest.converter.valuejson.ValueJsonConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringConverterFactory implements IStringConverterFactory {

    private Map<Class<?>, ValueJsonConverter> convertersByJavaType;
    private ValueJsonConverter defaultConverter;

    // these are explicit overrides for named attributes
    private Map<String, ValueJsonConverter> convertersByPath;

    public StringConverterFactory(
            Map<Class<?>, ValueJsonConverter> knownConverters,
            ValueJsonConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
        this.convertersByPath = new ConcurrentHashMap<>();
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

    /**
     * @since 2.11
     */
    protected ValueJsonConverter buildConverter(AgAttribute attribute) {
        return buildConverter(attribute.getType());
    }

    /**
     * @since 2.11
     */
    protected ValueJsonConverter buildConverter(Class<?> javaType) {
        return convertersByJavaType.getOrDefault(javaType, defaultConverter);
    }
}
