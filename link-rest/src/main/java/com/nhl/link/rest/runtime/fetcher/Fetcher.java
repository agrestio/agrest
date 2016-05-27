package com.nhl.link.rest.runtime.fetcher;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A common interface for data fetchers.
 * 
 * @since 2.0
 * @see FetcherBuilder
 */
@FunctionalInterface
public interface Fetcher<T, P> {

	Iterable<T> fetch(SelectContext<T> context, Iterable<P> parents);

	default Iterable<T> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
