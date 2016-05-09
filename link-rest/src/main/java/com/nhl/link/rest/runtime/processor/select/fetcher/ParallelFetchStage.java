package com.nhl.link.rest.runtime.processor.select.fetcher;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A {@link ProcessingStage} whose goal is to initialize {@link SelectContext}
 * data objects. As the name implies this stage is run in parallel by executing
 * multiple parallel fetchers, merging the results into a single object tree
 * that will be returned to the following stages.
 * 
 * @since 2.0
 */
public class ParallelFetchStage<T> extends BaseLinearProcessingStage<SelectContext<T>, T> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParallelFetchStage.class);

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
		FutureResult<T> rootResult = new FutureResult<>(fetcher, rootFuture);

		Set<FutureResult<?>> resultAccummulator = ConcurrentHashMap.newKeySet();
		fetcher.subFetchers().forEach(f -> executeSubFetcher(f, rootFuture, resultAccummulator));

		// ensure all fetchers have finished... (using "count()" at the end to
		// ensure the stream is run)
		resultAccummulator.stream().map(FutureResult::get).count();

		return rootResult.get();
	}

	protected <C, P> void executeSubFetcher(ChildFetcher<C, P> fetcher, Future<List<P>> parents,
			Set<FutureResult<?>> resultAccummulator) {

		Future<List<C>> result = executor.submit(() -> fetcher.fetch(parents));
		resultAccummulator.add(new FutureResult<>(fetcher, result));
		fetcher.subFetchers().forEach(f -> executeSubFetcher(f, result, resultAccummulator));
	}

	class FutureResult<R> {

		private HierarchicalFetcher<R> fetcher;
		private Future<List<R>> futureResult;

		public FutureResult(HierarchicalFetcher<R> fetcher, Future<List<R>> futureResult) {
			this.fetcher = fetcher;
			this.futureResult = futureResult;
		}

		public List<R> get() {

			// TODO: more descriptive exception message - which fetcher failed?

			try {
				return futureResult.get(singleFetcherTimeout, singleFetcherTimeoutUnit);
			} catch (InterruptedException e) {
				LOGGER.warn("fetcher interrupted: " + e.getMessage());
				return fetcher.onError(e);
			} catch (ExecutionException e) {
				LOGGER.warn("fetcher error: " + e.getMessage());
				return fetcher.onError(e);
			} catch (TimeoutException e) {
				LOGGER.warn("fetcher timed out");
				return fetcher.onError(e);
			}

		}
	}

}
