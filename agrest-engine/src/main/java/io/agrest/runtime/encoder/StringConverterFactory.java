package io.agrest.runtime.encoder;

import io.agrest.AgException;
import io.agrest.encoder.converter.GenericConverter;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringConverterFactory implements IStringConverterFactory {

    private Map<Class<?>, StringConverter> convertersByJavaType;
    private StringConverter defaultConverter;

    // these are explicit overrides for named attributes
    private Map<String, StringConverter> convertersByPath;

    public StringConverterFactory(
            Map<Class<?>, StringConverter> knownConverters,
            StringConverter defaultConverter) {

        this.convertersByJavaType = knownConverters;
        this.defaultConverter = defaultConverter;
        this.convertersByPath = new ConcurrentHashMap<>();
    }

    @Override
    public StringConverter getConverter(AgEntity<?> entity) {
        return getConverter(entity, null);
    }

    @Override
    public StringConverter getConverter(AgEntity<?> entity, String attributeName) {
        String key = attributeName != null ? entity.getName() + "." + attributeName : entity.getName();
        return convertersByPath.computeIfAbsent(key, k -> buildConverter(entity, attributeName));
    }

    protected StringConverter buildConverter(AgEntity<?> entity, String attributeName) {

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
    protected StringConverter buildConverter(AgAttribute attribute) {
        return buildConverter(attribute.getType());
    }

    /**
     * @since 2.11
     */
    protected StringConverter buildConverter(Class<?> javaType) {
        return convertersByJavaType.getOrDefault(javaType, defaultConverter);
    }
}
