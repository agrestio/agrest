package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.parser.converter.Base64Converter;
import com.nhl.link.rest.parser.converter.GenericConverter;
import com.nhl.link.rest.parser.converter.JsonValueConverter;
import com.nhl.link.rest.parser.converter.LongConverter;
import com.nhl.link.rest.parser.converter.UtcDateConverter;
import org.apache.cayenne.dba.TypesMapping;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 1.10
 */
public class DefaultJsonValueConverterFactory implements IJsonValueConverterFactory {

	protected Map<String, JsonValueConverter> convertersByJavaTypeName;
	protected Map<Class<?>, JsonValueConverter> convertersByJavaType;
	private Map<Integer, JsonValueConverter> convertersByJdbcType;

	private JsonValueConverter defaultConverter;

	public DefaultJsonValueConverterFactory() {

		this.defaultConverter = GenericConverter.converter();

		this.convertersByJavaTypeName = new HashMap<>();
		convertersByJavaTypeName.put(Long.class.getName(), LongConverter.converter());
		convertersByJavaTypeName.put(Date.class.getName(), UtcDateConverter.converter());
		convertersByJavaTypeName.put(java.sql.Date.class.getName(), UtcDateConverter.converter());
		convertersByJavaTypeName.put(java.sql.Time.class.getName(), UtcDateConverter.converter());
		convertersByJavaTypeName.put(java.sql.Timestamp.class.getName(), UtcDateConverter.converter());
		convertersByJavaTypeName.put(TypesMapping.JAVA_BYTES, Base64Converter.converter());

		this.convertersByJavaType = new HashMap<>();
		convertersByJavaType.put(Long.class, LongConverter.converter());
		convertersByJavaType.put(Date.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Date.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Time.class, UtcDateConverter.converter());
		convertersByJavaType.put(java.sql.Timestamp.class, UtcDateConverter.converter());
		convertersByJavaType.put(byte[].class, Base64Converter.converter());

		this.convertersByJdbcType = new HashMap<>();
		convertersByJdbcType.put(Types.BIGINT, LongConverter.converter());
		convertersByJdbcType.put(Types.TIMESTAMP, UtcDateConverter.converter());
	}

	@Override
	public JsonValueConverter converter(String valueType) {
		JsonValueConverter converter = convertersByJavaTypeName.get(valueType);
		return converter != null ? converter : defaultConverter;
	}

	@Override
	public JsonValueConverter converter(Class<?> valueType) {
		JsonValueConverter converter = convertersByJavaType.get(valueType);
		return converter != null ? converter : defaultConverter;
	}

	@Override
	public JsonValueConverter converter(int jdbcType) {
		JsonValueConverter converter = convertersByJdbcType.get(jdbcType);
		return converter != null ? converter : defaultConverter;
	}
}
