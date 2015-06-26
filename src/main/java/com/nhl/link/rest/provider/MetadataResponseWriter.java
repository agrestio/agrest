package com.nhl.link.rest.provider;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.MetadataResponse;

/**
 * @since 1.18
 */
public class MetadataResponseWriter extends BaseResponseWriter<MetadataResponse<?>> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return MetadataResponse.class.isAssignableFrom(type);
	}

	@Override
	protected void writeData(MetadataResponse<?> t, JsonGenerator out) throws IOException, LinkRestException {
		t.writeData(out);
	}

}
