package com.nhl.link.rest.meta;

/**
 * @since 1.12
 */
public interface LrDataMap {

	<T> LrEntity<T> getEntity(Class<T> type);
}
