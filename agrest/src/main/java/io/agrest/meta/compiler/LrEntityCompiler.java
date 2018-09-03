package io.agrest.meta.compiler;

import io.agrest.meta.LrDataMap;
import io.agrest.meta.LrEntity;

/**
 * @since 1.24
 */
public interface LrEntityCompiler {

	/**
	 * @since 2.0
     */
	<T> LrEntity<T> compile(Class<T> type, LrDataMap dataMap);
}
