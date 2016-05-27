package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A fetcher interface for queries that need to execute once per parent.
 * <p>
 * Generally this fetcher is fairly slow. First it will block until the parent
 * data is available. And then may potentially generate too many queries. The
 * last problem may be somewhat alleviated by running fetchers for each parent
 * in parallel.
 * 
 * @since 2.0
 * @see FetcherBuilder#batch(ParentAgnosticFetcher)
 */
public interface PerParentFetcher<T, P> {

	Iterable<T> fetch(SelectContext<T> context, P parent);
}
