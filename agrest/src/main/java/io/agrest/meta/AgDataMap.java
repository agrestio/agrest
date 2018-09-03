package io.agrest.meta;

/**
 * @since 1.12
 */
public interface AgDataMap {

	<T> AgEntity<T> getEntity(Class<T> type);
}
