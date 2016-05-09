package com.nhl.link.rest.runtime.processor.select;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.fetcher.FutureList;
import com.nhl.link.rest.runtime.fetcher.RootFetcher;
import com.nhl.link.rest.runtime.fetcher.SubFetcher;

/**
 * A {@link ProcessingStage} whose goal is to initialize {@link SelectContext}
 * data objects. As the name implies this stage is run in parallel by executing
 * multiple parallel fetchers, merging the results into a single object tree
 * that will be returned to the following stages.
 * 
 * @since 2.0
 */
public class ParallelFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private ExecutorService executor;
	private long singleFetcherTimeout;
	private TimeUnit singleFetcherTimeoutUnit;

	private Function<SelectContext<T>, RootFetcher<T>> fetchPartitioner;

	public ParallelFetchStage(ProcessingStage<SelectContext<T>, ? super T> next,
			Function<SelectContext<T>, RootFetcher<T>> fetchPartitioner, ExecutorService executor,
			long singleFetcherTimeout, TimeUnit singleFetcherTimeoutUnit) {

		super(next);

		this.executor = executor;
		this.fetchPartitioner = fetchPartitioner;
		this.singleFetcherTimeout = singleFetcherTimeout;
		this.singleFetcherTimeoutUnit = singleFetcherTimeoutUnit;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {

		List<T> rootData;
		RootFetcher<T> rootFetcher = fetchPartitioner.apply(context);

		if (rootFetcher.subFetchers().isEmpty()) {
			// single fetcher - run in the current thread...
			rootData = rootFetcher.fetch();
		} else {
			// fetcher hierarchy - run multithreaded in the background...
			rootData = executeRootFetcher(rootFetcher);
		}

		context.setObjects(rootData);
	}

	protected List<T> executeRootFetcher(RootFetcher<T> fetcher) {

		Future<List<T>> rootFuture = executor.submit(() -> fetcher.fetch());
		FutureList<T> rootResult = new FutureList<>(fetcher, rootFuture, singleFetcherTimeout,
				singleFetcherTimeoutUnit);

		Set<FutureList<?>> resultAccummulator = ConcurrentHashMap.newKeySet();
		fetcher.subFetchers().forEach(f -> executeSubFetcher(f, rootResult, resultAccummulator));

		// ensure all fetchers have finished... (using "count()" at the end to
		// ensure the stream is run)
		resultAccummulator.stream().map(FutureList::get).count();

		return rootResult.get();
	}

	protected <C, P> void executeSubFetcher(SubFetcher<C, P> fetcher, FutureList<P> parents,
			Set<FutureList<?>> resultAccummulator) {

		Future<List<C>> future = executor.submit(() -> fetcher.fetch(parents));
		FutureList<C> result = new FutureList<>(fetcher, future, singleFetcherTimeout, singleFetcherTimeoutUnit);
		resultAccummulator.add(result);
		fetcher.subFetchers().forEach(f -> executeSubFetcher(f, result, resultAccummulator));
	}

}
