package com.nhl.link.rest.runtime.parser;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @since 1.5
 */
public abstract class BaseRequestProcessor {

	protected static String string(MultivaluedMap<String, String> parameters, String name) {
		return parameters.getFirst(name);
	}

	protected static List<String> strings(MultivaluedMap<String, String> parameters, String name) {
		List<String> result = parameters.get(name);
		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

	protected static int integer(MultivaluedMap<String, String> parameters, String name) {

		String value = parameters.getFirst(name);
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
