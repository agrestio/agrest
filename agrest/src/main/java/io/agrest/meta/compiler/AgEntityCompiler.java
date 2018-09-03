package io.agrest.meta.compiler;

import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;

/**
 * @since 1.24
 */
public interface AgEntityCompiler {

	/**
	 * @since 2.0
     */
	<T> AgEntity<T> compile(Class<T> type, AgDataMap dataMap);
}
