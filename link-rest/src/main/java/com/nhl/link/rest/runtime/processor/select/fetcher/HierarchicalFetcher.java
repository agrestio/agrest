package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

/**
 * @since 2.0
 */
public interface HierarchicalFetcher<T> {
	
	default Collection<ChildFetcher<?, T>> subFetchers() {
		return Collections.emptyList();
	}

	default List<T> onError(Exception e) {
		throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching data", e);
	}
}
