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
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JsonConvertable;

/**
 * @since 1.18
 */
public class MetadataResponseWriter implements MessageBodyWriter<MetadataResponse<?>> {

	private IJacksonService jacksonService;

	@Context
	private Configuration configuration;

	@Override
	public long getSize(MetadataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MetadataResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(final MetadataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		getJacksonService().outputJson(new JsonConvertable() {
			@Override
			public void generateJSON(JsonGenerator out) throws IOException {
				out.writeStartObject();

				writeData(t, out);

				out.writeEndObject();
			}
		}, entityStream);
	}

	protected void writeData(MetadataResponse<?> t, JsonGenerator out) throws IOException, LinkRestException {
		t.writeData(out);
	}

	private IJacksonService getJacksonService() {
		if (jacksonService == null) {
			jacksonService = LinkRestRuntime.service(IJacksonService.class, configuration);
		}

		return jacksonService;
	}

}
