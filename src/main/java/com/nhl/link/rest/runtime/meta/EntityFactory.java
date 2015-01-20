package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.12
 */
public interface EntityFactory {

	<T> LrEntity<T> createEntity(Class<T> type);
}
