package io.agrest.encoder;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.ResourceEntity;
import io.agrest.meta.LrEntity;
import io.agrest.runtime.meta.IMetadataService;

/**
 * A superclass of authorizing {@link EncoderFilter}s that performs filter
 * matching based on the entity name.
 * 
 * @since 1.2
 */
public abstract class EntityEncoderFilter<T> implements EncoderFilter {

	private LrEntity<T> lrEntity;

	public EntityEncoderFilter(IMetadataService metadataService) {
		this.lrEntity = metadataService.getLrEntity(getType());
	}

	protected abstract Class<T> getType();

	protected abstract boolean willEncode(T object);

	@Override
	public boolean matches(ResourceEntity<?> resourceEntity) {
		return lrEntity == resourceEntity.getLrEntity();
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
