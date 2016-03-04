package com.nhl.link.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

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
	private ResourceEntity<T> entity;

	public MetadataResponse(Class<T> type) {
		this.encoder = GenericEncoder.encoder();
		this.type = type;
		this.resources = new ArrayList<>();
	}

	public Class<T> getType() {
		return type;
	}

	public MetadataResponse<T> resourceEntity(ResourceEntity<T> entity) {
		this.entity = entity;
		return this;
	}

	public MetadataResponse<T> withEncoder(Encoder encoder) {
		this.encoder = encoder;
		return this;
	}

	public MetadataResponse<T> withResources(Collection<LrResource<T>> resources) {
		this.resources.addAll(resources);
		return this;
	}

	public MetadataResponse<T> withResource(LrResource<T> resource) {
		this.resources.add(resource);
		return this;
	}

	public void writeData(JsonGenerator out) throws IOException {
		encoder.encode(null, resources, out);
	}

	public ResourceEntity<T> getEntity() {
		return entity;
	}

}
