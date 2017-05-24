package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.parser.converter.Base64Converter;
import com.nhl.link.rest.parser.converter.FloatConverter;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateConverter;
import com.nhl.link.rest.parser.converter.ISOLocalDateTimeConverter;
import com.nhl.link.rest.parser.converter.ISOLocalTimeConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.LongConverter;
import com.nhl.link.rest.parser.converter.UtcDateConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.10
 */
public class DefaultJsonValueConverterFactory implements IJsonValueConverterFactory {

	protected Map<Class<?>, JsonValueConverter> convertersByJavaType;

	private JsonValueConverter defaultConverter;

	public DefaultJsonValueConverterFactory() {

		this.defaultConverter = GenericConverter.converter();

		this.convertersByJavaType = new HashMap<>();
		convertersByJavaType.put(Object.class, GenericConverter.converter());
		convertersByJavaType.put(Float.class, FloatConverter.converter());
		convertersByJavaType.put(float.class, FloatConverter.converter());
		convertersByJavaType.put(Long.class, LongConverter.converter());
		convertersByJavaType.put(long.class, LongConverter.converter());
		convertersByJavaType.put(Date.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Date.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Time.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Timestamp.class, UtcDateConverter.converter());
		convertersByJavaType.put(byte[].class, Base64Converter.converter());
		convertersByJavaType.put(LocalDate.class, ISOLocalDateConverter.converter());
		convertersByJavaType.put(LocalTime.class, ISOLocalTimeConverter.converter());
		convertersByJavaType.put(LocalDateTime.class, ISOLocalDateTimeConverter.converter());
	}

	@Override
	public JsonValueConverter converter(Class<?> valueType) {
		JsonValueConverter converter = convertersByJavaType.get(valueType);
		return converter != null ? converter : defaultConverter;
	}

	@Override
	public JsonValueConverter converter(LrAttribute attribute) {
		return converter(attribute.getType());
	}

	@Override
	public JsonValueConverter converter(LrEntity<?> entity) {
		int ids = entity.getIds().size();
		if (ids != 1) {
			throw new IllegalArgumentException("Entity '" + entity.getName() +
					"' has unexpected number of ID attributes: " + ids);
		}
		return converter(entity.getIds().iterator().next().getType());
	}
}
