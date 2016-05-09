package com.nhl.link.rest.runtime.fetcher;

import java.util.List;

/**
 * @since 2.0
 */
@FunctionalInterface
public interface SubFetcher<T, P> extends Fetcher<T> {

	List<T> fetch(FutureList<P> parents);
}
