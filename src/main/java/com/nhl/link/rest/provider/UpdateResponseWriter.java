package com.nhl.link.rest.provider;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
public class UpdateResponseWriter extends BaseResponseWriter<UpdateResponse<?>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return UpdateResponse.class.isAssignableFrom(type);
	}

	@Override
	protected void writeData(UpdateResponse<?> t, JsonGenerator out) throws IOException {
		if (t.isIncludeData()) {
			t.writeData(out);
		}
	}
}
