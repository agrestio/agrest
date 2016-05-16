package com.nhl.link.rest.runtime.fetcher;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * A common interface for data fetchers.
 * 
 * @since 2.0
 */
public interface Fetcher<T> {

	Iterable<T> fetch();

	default Iterable<T> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
