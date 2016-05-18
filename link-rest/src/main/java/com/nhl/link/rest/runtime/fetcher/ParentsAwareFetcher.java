package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

public interface ParentsAwareFetcher<T, P> {

	Iterable<T> fetch(SelectContext<T> context, Iterable<P> parents);
}
