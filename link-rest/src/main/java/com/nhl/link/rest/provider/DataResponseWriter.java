package com.nhl.link.rest.provider;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.jackson.JsonConvertable;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class DataResponseWriter implements MessageBodyWriter<DataResponse<?>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return DataResponse.class.isAssignableFrom(type);
	}

	private IJacksonService jacksonService;

	@Context
	private Configuration configuration;

	@Override
	public long getSize(DataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType) {
		return -1;
	}

	@Override
	public void writeTo(final DataResponse<?> t, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException {

		getJacksonService().outputJson(new JsonConvertable() {
			@Override
			public void generateJSON(JsonGenerator out) throws IOException {
				writeData(t, out);
			}
		}, entityStream);
	}

	private IJacksonService getJacksonService() {
		if (jacksonService == null) {
			jacksonService = LinkRestRuntime.service(IJacksonService.class, configuration);
		}

		return jacksonService;
	}

	protected void writeData(DataResponse<?> t, JsonGenerator out) throws IOException {
		t.writeData(out);
	}

}
