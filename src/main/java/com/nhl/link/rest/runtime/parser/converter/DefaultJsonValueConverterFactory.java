package com.nhl.link.rest.runtime.parser.converter;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.LongConverter;
import com.nhl.link.rest.parser.converter.UtcDateConverter;

/**
 * @since 1.10
 */
public class DefaultJsonValueConverterFactory implements IJsonValueConverterFactory {

	private Map<String, JsonValueConverter> convertersByJavaType;
	private Map<Integer, JsonValueConverter> convertersByJdbcType;

	private JsonValueConverter defaultConverter;

	public DefaultJsonValueConverterFactory() {

		this.defaultConverter = GenericConverter.converter();

		this.convertersByJavaType = new HashMap<>();
		convertersByJavaType.put(Long.class.getName(), LongConverter.converter());
		convertersByJavaType.put(Date.class.getName(), UtcDateConverter.converter());

		this.convertersByJdbcType = new HashMap<>();
		convertersByJdbcType.put(Types.BIGINT, LongConverter.converter());
		convertersByJdbcType.put(Types.TIMESTAMP, UtcDateConverter.converter());
	}

	@Override
	public JsonValueConverter converter(String valueType) {
		JsonValueConverter converter = convertersByJavaType.get(valueType);
		return converter != null ? converter : defaultConverter;
	}

	@Override
	public JsonValueConverter converter(int jdbcType) {
		JsonValueConverter converter = convertersByJdbcType.get(jdbcType);
		return converter != null ? converter : defaultConverter;
	}
}
