package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A fetcher interface for queries that can be executed without the knowledge of
 * parent objects, and that can be "mapped" to parent objects based on their own
 * values.
 * <p>
 * This fetcher has very good parallelism, as it can be run in parallel with the
 * parent fetcher.
 * 
 * @since 2.0
 * @see FetcherBuilder#parentAgnostic(ParentAgnosticFetcher)
 */
public interface ParentAgnosticFetcher<T, P, I> {

	Iterable<T> fetch(SelectContext<T> context);
}
