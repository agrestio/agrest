package com.nhl.link.rest.encoder;

import com.nhl.link.rest.runtime.meta.IMetadataService;

/**
 * @deprecated since 1.2 use {@link EntityEncoderFilter} and override
 *             {@link #willEncode(Object)}.
 */
public abstract class NoRolesEntityAuthorizationEncoderFilter<T> extends EntityEncoderFilter<T> {

	public NoRolesEntityAuthorizationEncoderFilter(IMetadataService metadataService) {
		super(metadataService);
	}

	protected abstract boolean authorize(T object);

	@Override
	protected boolean willEncode(T object) {
		return authorize(object);
	}
}
