package com.nhl.link.rest.parser.converter;

import java.util.HashMap;
import java.util.Map;

/**
 * A utility class that ensures that client-provided numeric values are
 * converted to the right numeric type. This is especially important for ID
 * values that are used in comparisons.
 * 
 * @since 1.12
 */
public class Normalizer {

	static interface TypeNormalizer {
		Object normalize(Object value);
	}

	@SuppressWarnings("serial")
	private static final Map<String, TypeNormalizer> NORMALIZERS = new HashMap<String, TypeNormalizer>() {
		{
			put(Long.class.getName(), new TypeNormalizer() {
				@Override
				public Object normalize(Object value) {

					if (!(value instanceof Number)) {
						return value;
					}

					if (value instanceof Long) {
						return value;
					}

					return ((Number) value).longValue();
				}
			});
		}
	};

	public static Object normalize(Object value, String numericType) {
		TypeNormalizer normalizer = NORMALIZERS.get(numericType);
		return normalizer == null ? value : normalizer.normalize(value);
	}
}
