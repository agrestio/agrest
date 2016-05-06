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
	private List<T> objects;
	private Encoder encoder;

	@SuppressWarnings({ "unchecked" })
	public static <T> DataResponse<T> forObject(T object) {

		if (object == null) {
			throw new NullPointerException("Null object");
		}

		Class<T> type = (Class<T>) object.getClass();
		return new DataResponse<>(type).withObjects(Collections.singletonList(object));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> DataResponse<T> forObjects(List<T> objects) {

		if (objects.isEmpty()) {
			return new DataResponse(Object.class);
		} else {
			Class<T> type = (Class<T>) objects.get(0).getClass();
			return new DataResponse<>(type).withObjects(objects);
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

	public DataResponse<T> withObjects(List<T> objects) {
		this.objects = objects;
		return this;
	}

	public DataResponse<T> withObject(T object) {
		this.objects = Collections.singletonList(object);
		return this;
	}

	/**
	 * Returns all objects returned from DB.
	 */
	public List<T> getObjects() {
		return objects;
	}

	public DataResponse<T> withEncoder(Encoder encoder) {
		this.encoder = encoder;
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
