package com.nhl.link.rest.runtime.fetcher;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * A common superinterface of fetchers.
 * 
 * @since 2.0
 */
public interface Fetcher<T> {

	List<T> fetch();

	default List<T> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
