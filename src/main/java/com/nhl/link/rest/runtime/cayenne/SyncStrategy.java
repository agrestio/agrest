package com.nhl.link.rest.runtime.cayenne;

import java.util.List;

/**
 * @since 1.7
 */
interface SyncStrategy<T> {

	List<T> sync();
}
