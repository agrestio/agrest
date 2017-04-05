package com.nhl.link.rest.runtime.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public abstract class BaseRequestProcessor {

	public static String string(Map<String, List<String>> parameters, String name) {

		List<String> strings = strings(parameters, name);
		return strings.isEmpty() ? null : strings.get(0);
	}

    public static List<String> strings(Map<String, List<String>> parameters, String name) {
		List<String> result = parameters.get(name);
		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

    public static int integer(Map<String, List<String>> parameters, String name) {

		List<String> strings = strings(parameters, name);
		String value =  strings.isEmpty() ? null : strings.get(0);

		if (value == null) {
			return -1;
		}

		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException nfex) {
			return -1;
		}
	}
}
