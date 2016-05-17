package com.nhl.link.rest.runtime.processor.select;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.fetcher.Fetcher;
import com.nhl.link.rest.runtime.fetcher.FutureIterable;

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
	private Fetcher defaultFetcher;

	public ParallelFetchStage(ProcessingStage<SelectContext<T>, ? super T> next, ExecutorService executor,
			long singleFetcherTimeout, TimeUnit singleFetcherTimeoutUnit, Fetcher defaultFetcher) {

		super(next);

		this.defaultFetcher = defaultFetcher;
		this.executor = executor;
		this.singleFetcherTimeout = singleFetcherTimeout;
		this.singleFetcherTimeoutUnit = singleFetcherTimeoutUnit;
	}

	@Override
	protected void doExecute(SelectContext<T> context) {
		ResourceEntity<T> rootEntity = Objects.requireNonNull(context.getEntity());
		fetch(context, rootEntity, 0);

		// note that we can return from this stage, while the result is still
		// being calculated...
	}

	protected <U> int fetch(SelectContext<T> rootContext, ResourceEntity<U> entity, int treeDepth) {

		int childFetchers = 0;
		for (ResourceEntity<?> childEntity : entity.getChildren().values()) {
			childFetchers += fetch(rootContext, childEntity, treeDepth + 1);
		}

		Fetcher fetcher = getFetcher(entity, treeDepth);

		if (fetcher != null) {

			// fetch strategy - if we are the root fetcher, and there were no
			// child fetchers, run in the main thread. Otherwise run using
			// executor...

			SelectContext<U> subcontext = createSubcontext(rootContext, entity, treeDepth);
			Iterable<U> objects = (childFetchers == 0 && treeDepth == 0) ? fetchSynchronously(fetcher, subcontext)
					: fetchAsynchronously(fetcher, subcontext);
			entity.setObjects(objects);

			return childFetchers + 1;
		} else {
			return childFetchers;
		}
	}

	protected Fetcher getFetcher(ResourceEntity<?> entity, int treeDepth) {

		Fetcher fetcher = entity.getFetcher();
		if (fetcher != null) {
			return fetcher;
		}

		// use default fetcher for the top-level entity, null for any other
		// level ("null" meaning the entity will be fetched as a part of the
		// parent entity
		if (treeDepth == 0) {
			return defaultFetcher;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	protected <U> SelectContext<U> createSubcontext(SelectContext<T> rootContext, ResourceEntity<U> entity,
			int treeDepth) {

		if (treeDepth == 0) {
			return (SelectContext<U>) rootContext;
		}

		SelectContext<U> subcontext = new SelectContext<>(entity.getType());
		subcontext.setEntity(entity);
		return subcontext;
	}

	protected <U> Iterable<U> fetchSynchronously(Fetcher fetcher, SelectContext<U> context) {
		return fetcher.fetch(context);
	}

	protected <U> Iterable<U> fetchAsynchronously(Fetcher fetcher, SelectContext<U> context) {
		Future<Iterable<U>> future = executor.submit(() -> fetcher.fetch(context));
		return FutureIterable.future(fetcher, future, singleFetcherTimeout, singleFetcherTimeoutUnit);
	}

}
