package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.meta.LrDataMap;
import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.24
 */
public interface LrEntityCompiler {

	/**
	 * @since 2.0
     */
	<T> LrEntity<T> compile(Class<T> type, LrDataMap dataMap);
}
