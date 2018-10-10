package io.agrest.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.SimpleResponse;
import io.agrest.runtime.AgRuntime;
import io.agrest.runtime.jackson.IJacksonService;
import io.agrest.runtime.jackson.JsonConvertable;

@Provider
public class SimpleResponseWriter implements MessageBodyWriter<SimpleResponse> {

	private IJacksonService jacksonService;

	@Context
	private Configuration configuration;

	@Override
	public long getSize(SimpleResponse t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SimpleResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(final SimpleResponse t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		getJacksonService().outputJson(new JsonConvertable() {
			@Override
			public void generateJSON(JsonGenerator out) throws IOException {
				out.writeStartObject();
				out.writeBooleanField("success", t.isSuccess());

				if (t.getMessage() != null) {
					out.writeStringField("message", t.getMessage());
				}

				out.writeEndObject();
			}
		}, entityStream);
	}

	private IJacksonService getJacksonService() {
		if (jacksonService == null) {
			jacksonService = AgRuntime.service(IJacksonService.class, configuration);
		}

		return jacksonService;
	}

}
