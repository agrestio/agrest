package com.nhl.link.rest.runtime.parser;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

enum RequestParams {
	limit, start, page, include, exclude, sort, dir, group, groupDir, filter, query, cayenneExp;

	String string(MultivaluedMap<String, String> parameters) {
		return parameters.getFirst(name());
	}

	List<String> strings(MultivaluedMap<String, String> parameters) {
		List<String> result = parameters.get(name());
		if (result == null) {
			result = Collections.emptyList();
		}

		return result;
	}

	int integer(MultivaluedMap<String, String> parameters) {

		String value = parameters.getFirst(name());
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
