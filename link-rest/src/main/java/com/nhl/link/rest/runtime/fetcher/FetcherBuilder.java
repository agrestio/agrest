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

public interface FetcherBuilder {

	public static <T, P, I> ParentAgnosticFetcherBuilder<T, P, I> parentAgnostic(
			ParentAgnosticFetcher<T, P, I> fetcher) {
		return new ParentAgnosticFetcherBuilder<>(fetcher);
	}

	public static class ParentAgnosticFetcherBuilder<T, P, I> {

		private ParentAgnosticFetcher<T, P, I> fetcher;
		private Function<T, I> parentIdMapper;
		private Function<P, I> idMapper;
		private BiConsumer<P, Iterable<T>> parentChildConnector;

		private ParentAgnosticFetcherBuilder(ParentAgnosticFetcher<T, P, I> fetcher) {
			this.fetcher = fetcher;
		}

		public Fetcher<T> build() {

			Objects.requireNonNull(fetcher);
			Objects.requireNonNull(idMapper);
			Objects.requireNonNull(parentChildConnector);
			Objects.requireNonNull(parentIdMapper);

			return (context, parents) -> {
				Iterable<T> result = fetcher.fetch(context);

				Iterator<T> childrenIt = result.iterator();
				if (childrenIt.hasNext()) {

					@SuppressWarnings("unchecked")
					Iterator<P> parentsIt = (Iterator<P>) parents.iterator();
					if (parentsIt.hasNext()) {
						connectToParents(childrenIt, parentsIt);
					}
				}

				return result;
			};
		}

		private void connectToParents(Iterator<T> children, Iterator<P> parents) {

			Map<I, Collection<T>> childrenByParentId = new HashMap<>();
			while (children.hasNext()) {
				T child = children.next();
				I key = parentIdMapper.apply(child);
				childrenByParentId.computeIfAbsent(key, k -> new ArrayList<>()).add(child);
			}

			StreamSupport.stream(Spliterators.spliteratorUnknownSize(parents, Spliterator.ORDERED), false)
					.forEach(parent -> {
						I id = idMapper.apply(parent);
						Collection<T> parentsChildren = childrenByParentId.get(id);
						parentChildConnector.accept(parent, parentsChildren);
					});
		}

		public ParentAgnosticFetcherBuilder<T, P, I> idMapper(Function<P, I> idMapper) {
			this.idMapper = idMapper;
			return this;
		}

		public ParentAgnosticFetcherBuilder<T, P, I> parentIdMapper(Function<T, I> parentIdMapper) {
			this.parentIdMapper = parentIdMapper;
			return this;
		}

		public ParentAgnosticFetcherBuilder<T, P, I> toManyConnector(BiConsumer<P, Iterable<T>> connector) {
			this.parentChildConnector = connector;
			return this;
		}

		public ParentAgnosticFetcherBuilder<T, P, I> toOneConnector(BiConsumer<P, T> connector) {
			parentChildConnector = (parent, childCollection) -> {

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

				connector.accept(parent, child);
			};
			return this;
		}

	}
}
