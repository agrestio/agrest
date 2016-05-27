package com.nhl.link.rest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;

/**
 * A response object that represents a 'Collection Document' from LinkRest
 * protocol.
 */
public class DataResponse<T> extends LrResponse {

	private Class<T> type;
	private Iterable<T> objects;
	private Encoder encoder;

	public static <T> DataResponse<T> forObject(T object) {

		if (object == null) {
			throw new NullPointerException("Null object");
		}

		return forObjects(Collections.singletonList(object));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> DataResponse<T> forObjects(List<T> objects) {

		if (objects.isEmpty()) {
			return new DataResponse(Object.class);
		} else {
			Class<T> type = (Class<T>) objects.get(0).getClass();
			DataResponse<T> response = new DataResponse<>(type);
			response.setObjects(objects);
			return response;
		}
	}

	public static <T> DataResponse<T> forType(Class<T> type) {
		return new DataResponse<>(type);
	}

	DataResponse(Class<T> type) {
		this.type = type;
		this.encoder = GenericEncoder.encoder();
		this.objects = Collections.emptyList();
	}

	public Class<T> getType() {
		return type;
	}

	public Encoder getEncoder() {
		return encoder;
	}

	/**
	 * @since 1.24
	 */
	public void setObjects(Iterable<T> objects) {
		this.objects = objects;
	}

	/**
	 * @deprecated since 1.24 in favor of {@link #setObjects(List)}.
	 */
	public DataResponse<T> withObjects(List<T> objects) {
		this.objects = objects;
		return this;
	}

	/**
	 * @since 1.24
	 */
	public void setObject(T object) {
		setObjects(Collections.singletonList(object));
	}

	/**
	 * @deprecated since 1.24 in favor of {@link #setObject(Object)}.
	 */
	public DataResponse<T> withObject(T object) {
		this.objects = Collections.singletonList(object);
		return this;
	}

	/**
	 * Returns all objects returned from DB.
	 */
	public Iterable<T> getObjects() {
		return objects;
	}

	/**
	 * @since 1.24
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @deprecated since 1.24 in favor of {@link #setEncoder(Encoder)}.
	 */
	public DataResponse<T> withEncoder(Encoder encoder) {
		setEncoder(encoder);
		return this;
	}

	/**
	 * Writes internal state to the provided JSON stream using the internal
	 * {@link Encoder}.
	 */
	public void writeData(JsonGenerator out) throws IOException {
		encoder.encode("data", getObjects(), out);
	}
}
