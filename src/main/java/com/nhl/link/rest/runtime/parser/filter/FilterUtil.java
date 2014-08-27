package com.nhl.link.rest.runtime.parser.filter;

/**
 * @since 1.5
 */
public class FilterUtil {

	public static String escapeValueForLike(String value) {
		int len = value.length();

		StringBuilder out = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			char c = value.charAt(i);
			if (c == '_' || c == '%') {
				out.append('\\');
			}

			out.append(c);
		}

		return out.toString();
	}

}
