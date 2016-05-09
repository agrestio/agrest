package com.nhl.link.rest.runtime.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * A common superinterface of fetchers.
 * 
 * @since 2.0
 */
public interface Fetcher<T> {

	default Collection<SubFetcher<?, T>> subFetchers() {
		return Collections.emptyList();
	}

	default List<T> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
