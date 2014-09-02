package com.nhl.link.rest.runtime.cayenne;

import java.util.Map;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResponseObjectMapper;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.7
 */
interface ObjectLocator {

	<T> Map<EntityUpdate, T> locate(UpdateResponse<T> response, ResponseObjectMapper<T> mapper);
}
