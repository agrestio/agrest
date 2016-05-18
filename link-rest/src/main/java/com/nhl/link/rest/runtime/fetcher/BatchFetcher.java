package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A fetcher interface for queries that can be executed with a collection of
 * parents. There maybe two flavors of the implementation - one that does not
 * require any information about the parents to retrieve the data, and another
 * one that does. The former flavor will be able to run in parallel with the
 * parent fetcher. Though the later can perhaps build a more efficient query,
 * filtering its dataset by parent ids.
 * 
 * @since 2.0
 * @see FetcherBuilder#batch(BatchFetcher)
 */
public interface BatchFetcher<T, P, I> {

	Iterable<T> fetch(SelectContext<T> context, Iterable<P> parents);
}
