package com.nhl.link.rest.multisource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/**
 * @since 2.0
 */
public class MultiSelectBuilder<T> {

    private static final Pattern SPLIT_PATH = Pattern.compile("\\.");
    private static final Logger LOGGER = LoggerFactory.getLogger(MultiSelectBuilder.class);

    private SelectBuilder<T> rootChain;
    private ExecutorService executor;
    private Collection<StandaloneChain<?>> standaloneChains;
    private Collection<ParentDependentChain<?>> parentDependentChains;

    MultiSelectBuilder(SelectBuilder<T> rootChain, ExecutorService executor) {
        this.rootChain = Objects.requireNonNull(rootChain);
        this.executor = Objects.requireNonNull(executor);
        this.standaloneChains = new ArrayList<>();
        this.parentDependentChains = new ArrayList<>();
    }

    public <U> MultiSelectBuilder<T> parallel(Supplier<U> fetcher, BiConsumer<List<T>, U> merger) {
        return parallel("", fetcher, merger);
    }

    /**
     * Adds a parallel fetch stage with fetcher that does not depend on the root
     * result.
     *
     * @param pathToParents a property path to reach parent entity of this fetcher within
     *                      root result tree. Can be an empty string, in which case root
     *                      entity T is the parent.
     * @param fetcher       data fetcher that can be executed without the need to access
     *                      root result.
     * @param merger        merges fetcher result into root result after both results
     *                      become available.
     * @param <U>           the type of result provided by this stage.
     * @return this instance.
     */
    public <U, P> MultiSelectBuilder<T> parallel(String pathToParents, Supplier<U> fetcher,
                                                 BiConsumer<List<P>, U> merger) {

        BiConsumer<DataResponse<T>, U> lazyMerger = (r, u) -> {

            @SuppressWarnings("unchecked")
            List<P> parents = (List<P>) r.getIncludedObjects(Object.class, pathToParents);

            if (!parents.isEmpty()) {
                merger.accept(parents, u);
            }
        };

        standaloneChains.add(new StandaloneChain<>(fetcher, lazyMerger));
        return this;
    }

    /**
     * Adds a fetcher stage that is executed after the root chain result is
     * available. Often such a stage uses root result to build its own optimized
     * query (e.g. fetch details of objects from root using an external source).
     *
     * @param pathToParents a property path to reach parent entity of this fetcher within
     *                      root result tree. Can be an empty string, in which case root
     *                      entity T is the parent.
     * @param fetcher       data fetcher that is executed after the parent data is
     *                      available. Parent data is passed as parameter to the fetcher.
     * @param merger        merges fetcher result into root result after both results
     *                      become available.
     * @param <U>           the type of result provided by this stage.
     * @param <P>           the type of parent entity consumed by this fetcher.
     * @return this instance.
     */
    public <U, P> MultiSelectBuilder<T> afterParent(String pathToParents,
                                                    BiFunction<List<P>, ResourceEntity<P>, U> fetcher, BiConsumer<List<P>, U> merger) {
        return afterParent(pathToParents, fetcher, merger, Integer.MAX_VALUE);
    }

    public <U> MultiSelectBuilder<T> afterParent(BiFunction<List<T>, ResourceEntity<T>, U> fetcher,
                                                 BiConsumer<List<T>, U> merger) {
        return afterParent(fetcher, merger, Integer.MAX_VALUE);
    }

    public <U> MultiSelectBuilder<T> afterParent(BiFunction<List<T>, ResourceEntity<T>, U> fetcher,
                                                 BiConsumer<List<T>, U> merger, int parentBatchSize) {
        return afterParent("", fetcher, merger, parentBatchSize);
    }

    public <U, P> MultiSelectBuilder<T> afterParent(String pathToParents,
                                                    BiFunction<List<P>, ResourceEntity<P>, U> fetcher, BiConsumer<List<P>, U> merger, int parentBatchSize) {

        SelectContext<T>[] contextHolder = new SelectContext[1];

        // TODO: Modifying SelectBuilder passed to us externally is bad.
        // What if it is reused between requests? Though most chains contain
        // request parameter bindings, so hopefully they are one-off.

        rootChain.stage(SelectStage.START, (SelectContext<T> c) -> contextHolder[0] = c);

        @SuppressWarnings("unchecked")
        Function<List<P>, U> curriedFetcher = (parents) -> {

            Objects.requireNonNull(contextHolder[0]);
            ResourceEntity<T> rootEntity = contextHolder[0].getEntity();
            ResourceEntity<?> subEntity = rootEntity;

            String[] paths = pathToParents == null || pathToParents.length() == 0 ? new String[0]
                    : SPLIT_PATH.split(pathToParents);

            for (String path : paths) {
                subEntity = Objects.requireNonNull(subEntity.getChild(path),
                        "Invalid entity for path component: " + path);
            }

            return fetcher.apply(parents, (ResourceEntity<P>) subEntity);
        };

        return afterParent(pathToParents, curriedFetcher, merger, parentBatchSize);
    }

    public <U, P> MultiSelectBuilder<T> afterParent(String pathToParents, Function<List<P>, U> fetcher,
                                                    BiConsumer<List<P>, U> merger) {
        return afterParent(pathToParents, fetcher, merger, Integer.MAX_VALUE);
    }

    public <U> MultiSelectBuilder<T> afterParent(Function<List<T>, U> fetcher, BiConsumer<List<T>, U> merger) {
        return afterParent(fetcher, merger, Integer.MAX_VALUE);
    }

    public <U> MultiSelectBuilder<T> afterParent(Function<List<T>, U> fetcher, BiConsumer<List<T>, U> merger,
                                                 int parentBatchSize) {

        return afterParent("", fetcher, merger, parentBatchSize);
    }

    public <U, P> MultiSelectBuilder<T> afterParent(String pathToParents, Function<List<P>, U> fetcher,
                                                    BiConsumer<List<P>, U> merger, int parentBatchSize) {

        // zero or negative == no batching
        double batchSizeDouble = (parentBatchSize <= 0) ? Integer.MAX_VALUE : parentBatchSize;

        // TODO: currently only waits for root fetcher; need support for nesting
        // of fetcher dependencies

        BiConsumer<DataResponse<T>, ResultWithParentsTuple<P, U>> lazyMerger = (r, u) -> {

            // TODO: support for Optional<U> fetcher, so that we don't have to
            // guess whether NULL result was intentional or not

            if (u.result != null && !u.parents.isEmpty()) {
                merger.accept(u.parents, u.result);
            }
        };

        @SuppressWarnings("unchecked")
        Function<DataResponse<T>, ResultWithParentsTuple<P, U>> lazyFetcher = r -> {

            List<P> parents = (List<P>) r.getIncludedObjects(Object.class, pathToParents);
            int batches = (int) Math.ceil(parents.size() / batchSizeDouble);

            if (batches <= 1) {
                return ResultWithParentsTuple.fetch(parents, fetcher);
            } else {

                // start batch fetchers...

                CompletableFuture<DataResponse<T>> combinedFuture = CompletableFuture.completedFuture(r);

                for (int i = 0; i < batches; i++) {

                    int start = i * parentBatchSize;
                    int end = i + 1 == batches ? parents.size() : start + parentBatchSize;
                    List<P> subParents = parents.subList(start, end);
                    CompletableFuture<DataResponse<T>> future = new StandaloneChain<>(
                            () -> ResultWithParentsTuple.fetch(subParents, fetcher), lazyMerger)
                            .buildChain(combinedFuture);

                    combinedFuture = combinedFuture.thenCombine(future, (r1, r2) -> r1);
                }

                // TODO: how do we control timeouts here?
                combinedFuture.join();

                // return an empty result for the combined data (as it will be
                // already merged)
                return new ResultWithParentsTuple<>();
            }
        };

        parentDependentChains.add(new ParentDependentChain<>(lazyFetcher, lazyMerger));
        return this;
    }

    public DataResponse<T> select(long timeout, TimeUnit timeoutUnit) {

        // TODO: can we properly cancel the tasks on timeout?
        try {
            return selectAsync().get(timeout, timeoutUnit);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.info("Async fetcher error", e);
            throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Error fetching games", e);
        }
    }

    public CompletableFuture<DataResponse<T>> selectAsync() {

        CompletableFuture<DataResponse<T>> rootFuture = CompletableFuture.supplyAsync(() -> rootChain.get(),
                executor);

        Collection<CompletableFuture<DataResponse<T>>> futures = new ArrayList<>();

        for (StandaloneChain<?> chain : standaloneChains) {
            futures.add(chain.buildChain(rootFuture));
        }

        for (ParentDependentChain<?> chain : parentDependentChains) {
            futures.add(chain.buildChain(rootFuture));
        }

        if (futures.isEmpty()) {
            return rootFuture;
        }

        // now return a combined future that completes when all the fetchers are
        // complete

        // all subchains merge to the root, so no need to include root in
        // 'combinedFuture'.

        CompletableFuture<DataResponse<T>> combinedFuture = null;

        for (CompletableFuture<DataResponse<T>> future : futures) {
            combinedFuture = combinedFuture == null ? future : combinedFuture.thenCombine(future, (r1, r2) -> r1);
        }

        return combinedFuture;
    }

    static class ResultWithParentsTuple<P, U> {

        List<P> parents;
        U result;

        static <P, U> ResultWithParentsTuple<P, U> fetch(List<P> parents, Function<List<P>, U> fetcher) {
            ResultWithParentsTuple<P, U> tuple = new ResultWithParentsTuple<>();
            tuple.parents = parents;

            if (!tuple.parents.isEmpty()) {
                tuple.result = fetcher.apply(tuple.parents);
            }

            return tuple;
        }
    }

    class StandaloneChain<U> {

        private Supplier<U> fetcher;
        private BiConsumer<DataResponse<T>, U> merger;

        public StandaloneChain(Supplier<U> fetcher, BiConsumer<DataResponse<T>, U> merger) {
            this.fetcher = Objects.requireNonNull(fetcher);
            this.merger = Objects.requireNonNull(merger);
        }

        CompletableFuture<DataResponse<T>> buildChain(CompletableFuture<DataResponse<T>> rootResult) {
            CompletableFuture<U> chainFuture = CompletableFuture.supplyAsync(fetcher, executor);

            BiFunction<DataResponse<T>, U, DataResponse<T>> mergerAsFunction = (r, u) -> {
                merger.accept(r, u);
                return r;
            };

            return rootResult.thenCombine(chainFuture, mergerAsFunction);
        }
    }

    class ParentDependentChain<U> {

        private Function<DataResponse<T>, U> fetcher;
        private BiConsumer<DataResponse<T>, U> merger;

        public ParentDependentChain(Function<DataResponse<T>, U> fetcher, BiConsumer<DataResponse<T>, U> merger) {
            this.fetcher = Objects.requireNonNull(fetcher);
            this.merger = Objects.requireNonNull(merger);
        }

        CompletableFuture<DataResponse<T>> buildChain(CompletableFuture<DataResponse<T>> rootResult) {

            Function<DataResponse<T>, CompletableFuture<U>> fetcherAsAsyncFunction = (r) -> {
                return CompletableFuture.supplyAsync(() -> {
                    return fetcher.apply(r);
                }, executor);
            };

            CompletableFuture<U> chainFuture = rootResult.thenCompose(fetcherAsAsyncFunction);

            BiFunction<DataResponse<T>, U, DataResponse<T>> mergerAsFunction = (r, u) -> {
                merger.accept(r, u);
                return r;
            };

            return rootResult.thenCombine(chainFuture, mergerAsFunction);
        }
    }

}
