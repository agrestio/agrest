package com.nhl.link.rest.runtime.parser;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhl.link.rest.LinkRestException;

class RequestJsonParser {

	private static final String[] EMPTY_ARRAY = new String[0];

	private JsonFactory jsonFactory;

	RequestJsonParser(JsonFactory jsonFactory) {
		this.jsonFactory = jsonFactory;
	}

	String[] parseJSONStringArray(String raw, ObjectMapper objectMapper) {

		if (raw == null || raw.length() == 0) {
			return EMPTY_ARRAY;
		}

		JsonNode rootNode = parseJSON(raw, objectMapper);
		if (!rootNode.isArray()) {
			return new String[] { rootNode.asText() };
		}

		String[] result = new String[rootNode.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = rootNode.get(i).asText();
		}

		return result;
	}

	JsonNode parseJSON(String raw, ObjectMapper objectMapper) {

		if (raw == null) {
			return null;
		}

		try {
			JsonParser parser = jsonFactory.createJsonParser(raw);
			return objectMapper.readTree(parser);
		} catch (IOException ioex) {
			throw new LinkRestException(Status.BAD_REQUEST, "Error parsing JSON");
		}
	}

}
