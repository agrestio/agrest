package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.List;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface RootFetcher<T> extends HierarchicalFetcher<T> {

	List<T> fetch();
}
