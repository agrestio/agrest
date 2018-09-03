package io.agrest.runtime.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;

public interface IJacksonService {

	JsonFactory getJsonFactory();

	void outputJson(JsonConvertable object, OutputStream out) throws IOException;

	/**
	 * @since 1.5
	 */
	JsonNode parseJson(String json);
	
	/**
	 * @since 1.20
	 */
	JsonNode parseJson(InputStream json);
}
