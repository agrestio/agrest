package com.nhl.link.rest.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JsonConvertable;

public abstract class BaseResponseWriter<T extends SimpleResponse> implements MessageBodyWriter<T> {

	private IJacksonService jacksonService;

	@Context
	private Configuration configuration;

	@Override
	public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	@Override
	public abstract boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType);

	@Override
	public void writeTo(final T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException {

		getJacksonService().outputJson(new JsonConvertable() {
			@Override
			public void generateJSON(JsonGenerator out) throws IOException {
				out.writeStartObject();
				out.writeBooleanField("success", t.isSuccess());

				if (t.getMessage() != null) {
					out.writeStringField("message", t.getMessage());
				}

				writeData(t, out);

				out.writeEndObject();
			}
		}, entityStream);
	}

	private IJacksonService getJacksonService() {
		if (jacksonService == null) {
			jacksonService = LinkRestRuntime.service(IJacksonService.class, configuration);
		}

		return jacksonService;
	}

	/**
	 * Provides a placehodler to override in subclasses if they need to
	 * serialize the "data" section of the response. This implementation does
	 * nothing.
	 */
	protected void writeData(T t, JsonGenerator out) throws IOException, LinkRestException {
		// noop
	}

}
