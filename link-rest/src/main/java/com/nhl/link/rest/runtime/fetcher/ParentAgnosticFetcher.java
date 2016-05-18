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

import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * A fetcher interface for queries that can be executed without the knowledge of
 * parent objects, and that can be "mapped" to parent objects based on their own
 * values.
 * <p>
 * This fetcher has very good parallelism, as it can be run in parallel with the
 * parent fetcher.
 * 
 * @since 2.0
 */
public interface ParentAgnosticFetcher<T, P, I> {

	Iterable<T> fetch(SelectContext<T> context);

	public static <T, P, I> Builder<T, P, I> builder(ParentAgnosticFetcher<T, P, I> fetcher) {
		return new Builder<>(fetcher);
	}

	public static class Builder<T, P, I> {

		private ParentAgnosticFetcher<T, P, I> fetcher;
		private Function<T, I> parentIdMapper;
		private Function<P, I> idMapper;
		private BiConsumer<P, Iterable<T>> parentChildConnector;

		private Builder(ParentAgnosticFetcher<T, P, I> fetcher) {
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

		public Builder<T, P, I> idMapper(Function<P, I> idMapper) {
			this.idMapper = idMapper;
			return this;
		}

		public Builder<T, P, I> parentIdMapper(Function<T, I> parentIdMapper) {
			this.parentIdMapper = parentIdMapper;
			return this;
		}

		public Builder<T, P, I> toManyConnector(BiConsumer<P, Iterable<T>> connector) {
			this.parentChildConnector = connector;
			return this;
		}

		public Builder<T, P, I> toOneConnector(BiConsumer<P, T> connector) {
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
