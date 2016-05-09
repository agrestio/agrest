package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.List;
import java.util.concurrent.Future;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface ChildFetcher<T, P> extends HierarchicalFetcher<T> {

	List<T> fetch(Future<List<P>> parents);
}
