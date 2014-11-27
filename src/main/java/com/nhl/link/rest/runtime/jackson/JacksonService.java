package com.nhl.link.rest.runtime.jackson;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.core.Response.Status;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.nhl.link.rest.LinkRestException;

public class JacksonService implements IJacksonService {

	private ObjectMapper sharedMapper;
	private JsonFactory sharedFactory;

	public JacksonService() {

		// fun Jackson API with circular dependencies ... so we create a mapper
		// first, and grab implicitly created factory from it
		this.sharedMapper = new ObjectMapper();
		this.sharedFactory = sharedMapper.getFactory();

		// make sure mapper does not attempt closing streams it does not
		// manage... why is this even a default in jackson?
		sharedFactory.disable(Feature.AUTO_CLOSE_TARGET);

		// do not flush every time. why would we want to do that?
		// this is having a HUGE impact on extrest serializers (5x speedup)
		sharedMapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
	}

	@Override
	public JsonFactory getJsonFactory() {
		return sharedFactory;
	}

	@Override
	public void outputJson(JsonConvertable processor, OutputStream out) throws IOException {
		// TODO: UTF-8 is hardcoded, it is likely we may have alt. encodings
		try (JsonGenerator generator = sharedFactory.createGenerator(out, JsonEncoding.UTF8)) {
			processor.generateJSON(generator);
		}
	}

	/**
	 * @since 1.5
	 */
	@Override
	public JsonNode parseJson(String json) {
		if (json == null) {
			return null;
		}

		try {
			JsonParser parser = getJsonFactory().createParser(json);
			return new ObjectMapper().readTree(parser);
		} catch (IOException ioex) {
			throw new LinkRestException(Status.BAD_REQUEST, "Error parsing JSON");
		}
	}
}
