package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * @since 2.0
 */
public interface Fetcher<C> {

	<P> List<C> fetch(Future<List<P>> parents);

	Collection<Fetcher<?>> subFetchers();

	default List<C> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
