package io.agrest.runtime.encoder;

import io.agrest.LinkRestException;
import io.agrest.encoder.converter.GenericConverter;
import io.agrest.encoder.converter.StringConverter;
import io.agrest.meta.AgAttribute;
import io.agrest.meta.AgEntity;

import javax.ws.rs.core.Response.Status;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringConverterFactory implements IStringConverterFactory {

	private Map<Class<?>, StringConverter> convertersByJavaType;
	private StringConverter defaultConverter;

	// these are explicit overrides for named attributes
	private Map<String, StringConverter> convertersByPath;

	public StringConverterFactory(Map<Class<?>, StringConverter> knownConverters,
								  StringConverter defaultConverter) {
		// creating a concurrent copy of the provided map - we'll be expanding it dynamically.
		this.convertersByJavaType = new ConcurrentHashMap<>(knownConverters);
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

		StringConverter converter = convertersByPath.get(key);
		if (converter == null) {
			converter = buildConverter(entity, attributeName);
			convertersByPath.put(key, converter);
		}

		return converter;
	}

	protected StringConverter buildConverter(AgEntity<?> entity, String attributeName) {

		if (attributeName == null) {
			// root object encoder... assuming we'll get ID as number
			return GenericConverter.converter();
		}

		AgAttribute attribute = entity.getAttribute(attributeName);

		if (attribute == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "Invalid attribute: '" + entity.getName() + "."
					+ attributeName + "'");
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
		return convertersByJavaType.computeIfAbsent(javaType, vt -> defaultConverter);
	}
}
