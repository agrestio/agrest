package com.nhl.link.rest.runtime.processor.meta;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.processor.BaseProcessingContext;

/**
 * @since 1.18
 */
public class MetadataContext<T> extends BaseProcessingContext<T> {

	private Class<?> resourceType;
	private UriInfo uriInfo;
	private Encoder encoder;
	private LrEntity<T> entity;
	private Collection<LrResource<T>> resources;

	public MetadataContext(Class<T> type) {
		super(type);
	}

	/**
	 * Returns a new response object reflecting the context state.
	 * 
	 * @since 1.24
	 * @return a newly created response object reflecting the context state.
	 */
	public MetadataResponse<T> createMetadataResponse() {
		MetadataResponse<T> response = new MetadataResponse<>(getType());
		response.setEncoder(encoder);
		response.setResources(resources);

		return response;
	}

	public LrEntity<T> getEntity() {
		return entity;
	}

	public void setEntity(LrEntity<T> entity) {
		this.entity = entity;
	}

	public void setResource(Class<?> resourceClass) {
		this.resourceType = resourceClass;
	}

	public Class<?> getResource() {
		return resourceType;
	}

	/**
	 * @since 1.24
	 */
	public Encoder getEncoder() {
		return encoder;
	}

	/**
	 * @since 1.24
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @since 1.24
	 */
	public Collection<LrResource<T>> getResources() {
		return resources;
	}

	/**
	 * @since 1.24
	 */
	public void setResources(Collection<LrResource<T>> resources) {
		this.resources = resources;
	}

	public UriInfo getUriInfo() {
		return uriInfo;
	}

	public void setUriInfo(UriInfo uriInfo) {
		this.uriInfo = uriInfo;
	}

	public String getApplicationBase() {
		if (uriInfo == null) {
			return null;
		}
		return uriInfo.getBaseUri().toString();
	}
}
