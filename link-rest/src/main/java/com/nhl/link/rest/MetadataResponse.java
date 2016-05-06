package com.nhl.link.rest;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.meta.LrResource;

/**
 * A response object that represents a 'Metadata Document' from LinkRest
 * protocol.
 * 
 * @since 1.18
 */
public class MetadataResponse<T> extends LrResponse {

	private Encoder encoder;
	private Collection<LrResource<T>> resources;
	private Class<T> type;

	public MetadataResponse(Class<T> type) {
		this.encoder = GenericEncoder.encoder();
		this.type = type;
		this.resources = Collections.emptyList();
	}

	public Class<T> getType() {
		return type;
	}

	/**
	 * @since 1.24
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @deprecated since 1.24 use {@link #setEncoder(Encoder)}.
	 */
	public MetadataResponse<T> withEncoder(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	/**
	 * @since 1.24
	 */
	public void setResources(Collection<LrResource<T>> resources) {
		this.resources = resources;
	}

	/**
	 * @since 1.24
	 */
	public void setResource(LrResource<T> resource) {
		setResources(Collections.singletonList(resource));
	}

	public void writeData(JsonGenerator out) throws IOException {
		encoder.encode(null, resources, out);
	}
}
