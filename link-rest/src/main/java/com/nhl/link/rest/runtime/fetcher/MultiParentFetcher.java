package com.nhl.link.rest.runtime.fetcher;

import java.util.Map;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

public interface MultiParentFetcher {

	<T> Map<Object, Iterable<T>> fetch(SelectContext<T> context, Iterable<?> parents);
}
