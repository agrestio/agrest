package com.nhl.link.rest.update;

import com.nhl.link.rest.UpdateResponse;

/**
 * A chainable filter of update responses.
 * 
 * @since 1.3
 */
public interface UpdateFilter {

	<T> UpdateResponse<T> afterParse(UpdateResponse<T> response);
}
