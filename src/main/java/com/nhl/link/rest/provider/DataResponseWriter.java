package com.nhl.link.rest.provider;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;

@Provider
public class DataResponseWriter extends BaseResponseWriter<DataResponse<?>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return DataResponse.class.isAssignableFrom(type);
	}

	@Override
	protected void writeData(DataResponse<?> t, JsonGenerator out) throws IOException {
		t.writeData(out);
	}

}
