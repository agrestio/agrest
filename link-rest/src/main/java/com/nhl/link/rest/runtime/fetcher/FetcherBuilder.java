package com.nhl.link.rest.runtime.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.StreamSupport;

/**
 * Produces fetchers that extract the data from some data sources and connect
 * fetched objects into a parent-child tree. Produced fetchers are usually based
 * on non-connecting {@link Fetcher} or {@link PerParentFetcher} with addition
 * of parent-child connection functionality.
 * 
 * @since 2.0
 */
public interface FetcherBuilder {

	/**
	 * Starts a builder for a batch fetcher.
	 * 
	 * @param nonConnectingFetcher
	 *            a fetcher that fetches the data, but does not attempt to
	 *            connect fetched objects to their parents.
	 * @param keyType
	 *            a java type of the key used to map children to parents.
	 * @return
	 */
	public static <T, P, K> BatchFetcherBuilder<T, P, K> batch(Fetcher<T, P> nonConnectingFetcher, Class<K> keyType) {
		return new BatchFetcherBuilder<>(nonConnectingFetcher);
	}

	public static <T, P> PerParentFetcherBuilder<T, P> perParent(PerParentFetcher<T, P> nonConnectingFetcher) {
		return new PerParentFetcherBuilder<>(nonConnectingFetcher);
	}

	public static class BatchFetcherBuilder<T, P, K> {

		private Fetcher<T, P> nonConnectingFetcher;
		private Function<T, K> childKeyMapper;
		private Function<P, K> parentKeyMapper;
		private BiConsumer<P, Iterable<T>> parentChildConnector;

		private BatchFetcherBuilder(Fetcher<T, P> nonConnectingFetcher) {
			this.nonConnectingFetcher = nonConnectingFetcher;
		}

		public Fetcher<T, P> build() {

			Objects.requireNonNull(nonConnectingFetcher);
			Objects.requireNonNull(parentKeyMapper);
			Objects.requireNonNull(parentChildConnector);
			Objects.requireNonNull(childKeyMapper);

			return (context, parents) -> {

				Iterable<T> result = nonConnectingFetcher.fetch(context, parents);

				Iterator<T> childrenIt = result.iterator();
				if (childrenIt.hasNext()) {

					Iterator<P> parentsIt = (Iterator<P>) parents.iterator();
					if (parentsIt.hasNext()) {
						connectToParents(childrenIt, parentsIt);
					}
				}

				return result;
			};
		}

		private void connectToParents(Iterator<T> children, Iterator<P> parents) {

			Map<K, Collection<T>> childrenByParentId = new HashMap<>();
			while (children.hasNext()) {
				T child = children.next();
				K key = childKeyMapper.apply(child);
				childrenByParentId.computeIfAbsent(key, k -> new ArrayList<>()).add(child);
			}

			StreamSupport.stream(Spliterators.spliteratorUnknownSize(parents, Spliterator.ORDERED), false)
					.forEach(parent -> {
						K id = parentKeyMapper.apply(parent);
						Collection<T> parentsChildren = childrenByParentId.get(id);
						parentChildConnector.accept(parent, parentsChildren);
					});
		}

		public BatchFetcherBuilder<T, P, K> parentKeyMapper(Function<P, K> keyMapper) {
			this.parentKeyMapper = keyMapper;
			return this;
		}

		public BatchFetcherBuilder<T, P, K> childKeyMapper(Function<T, K> keyMapper) {
			this.childKeyMapper = keyMapper;
			return this;
		}

		public BatchFetcherBuilder<T, P, K> toManyConnector(BiConsumer<P, Iterable<T>> connector) {
			this.parentChildConnector = connector;
			return this;
		}

		public BatchFetcherBuilder<T, P, K> toOneConnector(BiConsumer<P, T> connector) {
			parentChildConnector = mapToOneConnector(connector);
			return this;
		}

		private static <P, T> BiConsumer<P, Iterable<T>> mapToOneConnector(BiConsumer<P, T> toOneConnector) {

			return (parent, childCollection) -> {

				Iterator<T> it = childCollection.iterator();

				T child;
				if (!it.hasNext()) {
					child = null;
				} else {
					child = it.next();

					if (it.hasNext()) {
						throw new IllegalArgumentException("Expecting single child.");
					}
				}

				toOneConnector.accept(parent, child);
			};
		}
	}

	public static class PerParentFetcherBuilder<T, P> {

		private PerParentFetcher<T, P> nonConnectingFetcher;
		private BiConsumer<P, Iterable<T>> parentChildConnector;

		private PerParentFetcherBuilder(PerParentFetcher<T, P> nonConnectingFetcher) {
			this.nonConnectingFetcher = nonConnectingFetcher;
		}

		public Fetcher<T, P> build() {

			Objects.requireNonNull(nonConnectingFetcher);
			Objects.requireNonNull(parentChildConnector);

			return (context, parents) -> {

				Collection<T> combinedResult = new ArrayList<>();

				parents.forEach(p -> {

					// TODO: must split into parallel fetchers on the same
					// executor as the main fetcher is running
					Iterable<T> result = nonConnectingFetcher.fetch(context, p);
					parentChildConnector.accept(p, result);

					result.forEach(t -> combinedResult.add(t));

				});

				return combinedResult;
			};
		}

		public PerParentFetcherBuilder<T, P> toManyConnector(BiConsumer<P, Iterable<T>> connector) {
			this.parentChildConnector = connector;
			return this;
		}

		public PerParentFetcherBuilder<T, P> toOneConnector(BiConsumer<P, T> connector) {
			parentChildConnector = BatchFetcherBuilder.mapToOneConnector(connector);
			return this;
		}

	}
}
