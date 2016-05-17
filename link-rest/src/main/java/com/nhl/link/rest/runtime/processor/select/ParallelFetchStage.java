package com.nhl.link.rest.runtime.processor.select;

import java.util.ArrayList;
import java.util.Collection;
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

		// fetch strategy - if we are the root fetcher, and there were no
		// child fetchers, run in the main thread. Otherwise run using
		// executor...

		if (hasChildFetchers(rootEntity)) {
			// TODO: should we use some kind of ForkJoinTask here?
			fetchRecursive(context, rootEntity, null, 0).forEach(FutureIterable::getOrAwaitResult);
		} else {
			fetchRoot(context);
		}
	}

	protected boolean hasChildFetchers(ResourceEntity<?> entity) {

		for (ResourceEntity<?> childEntity : entity.getChildren().values()) {
			if (childEntity.getFetcher() != null || hasChildFetchers(childEntity)) {
				return true;
			}
		}

		return false;
	}

	protected <U> Collection<FutureIterable<?>> fetchRecursive(SelectContext<T> rootContext, ResourceEntity<U> entity,
			FutureIterable<?> parentResult, int treeDepth) {

		Collection<FutureIterable<?>> allResults = new ArrayList<>();
		Fetcher fetcher = getFetcher(entity, treeDepth);
		FutureIterable<?> results;

		if (fetcher != null) {

			@SuppressWarnings("unchecked")
			SelectContext<U> subcontext = treeDepth > 0 ? createSubcontext(rootContext, entity, treeDepth)
					: (SelectContext<U>) rootContext;

			Future<Iterable<U>> future = executor.submit(() -> fetcher.fetch(subcontext, parentResult));
			results = FutureIterable.future(fetcher, future, singleFetcherTimeout, singleFetcherTimeoutUnit);
			allResults.add(results);
		} else {
			// TODO: for non-fetching nodes we probably need to fake the result
			// that is a parent of U... for now it may be a parent of a parent
			// of a parent
			results = parentResult;
		}

		for (ResourceEntity<?> childEntity : entity.getChildren().values()) {
			allResults.addAll(fetchRecursive(rootContext, childEntity, results, treeDepth + 1));
		}

		return allResults;
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

	protected void fetchRoot(SelectContext<T> context) {
		Fetcher fetcher = getFetcher(context.getEntity(), 0);
		fetcher.fetch(context, null);
	}

}
