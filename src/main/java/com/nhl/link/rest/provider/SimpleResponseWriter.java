package com.nhl.link.rest.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import com.nhl.link.rest.SimpleResponse;

@Provider
public class SimpleResponseWriter extends BaseResponseWriter<SimpleResponse> {

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return SimpleResponse.class.isAssignableFrom(type);
	}
}
