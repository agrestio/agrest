package com.nhl.link.rest.runtime.parser.converter;

import com.nhl.link.rest.parser.converter.JsonValueConverter;

/**
 * A service that ensures proper conversion of incoming JSON values to the
 * model-compatible Java types.
 * 
 * @since 1.10
 */
public interface IJsonValueConverterFactory {

	JsonValueConverter converter(String valueType);
	
	JsonValueConverter converter(int jdbcType);
}
