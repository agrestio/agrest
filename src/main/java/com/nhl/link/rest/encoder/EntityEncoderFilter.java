package com.nhl.link.rest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.IMetadataService;

/**
 * A superclass of authorizing {@link EncoderFilter}s that performs filter
 * matching based on the entity name.
 * 
 * @since 1.2
 */
public abstract class EntityEncoderFilter<T> implements EncoderFilter {

	private LrEntity<T> entity;

	public EntityEncoderFilter(IMetadataService metadataService) {
		this.entity = metadataService.getLrEntity(getType());
	}

	protected abstract Class<T> getType();

	protected abstract boolean willEncode(T object);

	@Override
	public boolean matches(ResourceEntity<?> clientEntity) {
		return entity == clientEntity.getLrEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException {

		if (willEncode((T) object)) {
			return delegate.encode(propertyName, object, out);
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean willEncode(String propertyName, Object object, Encoder delegate) {
		return willEncode((T) object);
	}
}
