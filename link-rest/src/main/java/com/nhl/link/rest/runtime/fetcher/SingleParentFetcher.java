package com.nhl.link.rest.runtime.fetcher;

import com.nhl.link.rest.runtime.processor.select.SelectContext;

public interface SingleParentFetcher {

	<T> Iterable<T> fetch(SelectContext<T> context, Object parent);
}
