package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 2.0
 */
public class ParallelFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private ExecutorService executor;
	private long timeoutMs;

	private Function<SelectContext<T>, Fetcher<T>> fetcherTreeBuilder;

	public ParallelFetchStage(ProcessingStage<SelectContext<T>, ? super T> next, ExecutorService executor,
			long timeoutMs) {
		super(next);
		this.executor = executor;
		this.timeoutMs = timeoutMs;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {

		List<T> rootData;
		Fetcher<T> rootFetcher = fetcherTreeBuilder.apply(context);

		if (rootFetcher.subFetchers().isEmpty()) {
			// single fetcher - run in the current thread...
			rootData = rootFetcher.fetch(null);
		} else {
			// multiple fetchers - run multithreaded in the background...

			rootData = executeFetcher(rootFetcher, null);

			// TODO: wait for all sub-fetchers to complete before continuing...
		}

		context.setObjects(rootData);
	}

	protected <C, P> List<C> executeFetcher(Fetcher<C> fetcher, Future<List<P>> parents) {

		Future<List<C>> result = executor.submit(() -> fetcher.fetch(null));
		fetcher.subFetchers().forEach(f -> executeFetcher(f, result));

		try {
			return result.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			return fetcher.onError(e);
		}
	}
}
