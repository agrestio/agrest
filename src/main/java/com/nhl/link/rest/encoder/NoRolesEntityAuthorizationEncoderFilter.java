package com.nhl.link.rest.encoder;

import java.io.IOException;

import org.apache.cayenne.map.ObjEntity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.Entity;
import com.nhl.link.rest.runtime.meta.IMetadataService;

/**
 * A superclass of authorizing {@link EncoderFilter}s that performs filter
 * matching based on the entity name.
 */
public abstract class NoRolesEntityAuthorizationEncoderFilter<T> implements EncoderFilter {

	private ObjEntity entity;

	public NoRolesEntityAuthorizationEncoderFilter(IMetadataService metadataService) {
		this.entity = metadataService.getObjEntity(getType());
	}

	protected abstract Class<T> getType();

	protected abstract boolean authorize(T object);

	@Override
	public boolean matches(Entity<?> clientEntity) {
		return entity == clientEntity.getCayenneEntity();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean encode(String propertyName, Object object, JsonGenerator out, Encoder delegate) throws IOException {

		if (authorize((T) object)) {
			return delegate.encode(propertyName, object, out);
		} else {
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean willEncode(String propertyName, Object object, Encoder delegate) {
		return authorize((T) object);
	}
}
