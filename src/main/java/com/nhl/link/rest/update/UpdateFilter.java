package com.nhl.link.rest.update;

import com.nhl.link.rest.UpdateResponse;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;

/**
 * A chainable filter of update responses.
 * 
 * @since 1.3
 * @deprecated since 1.19. Use a listener with {@link UpdateRequestParsed} or
 *             another update listener annotation.
 */
@Deprecated
public interface UpdateFilter {

	<T> UpdateResponse<T> afterParse(UpdateResponse<T> response);
}
