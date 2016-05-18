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

public class TreeConnectingFetcher implements Fetcher {

	private Fetcher delegateFetcher;
	private Function<Object, Object> parentIdMapper;
	private Function<Object, ?> idMapper;
	private BiConsumer<Object, Collection<Object>> parentChildConnector;

	public static Builder builder() {
		return new Builder();
	}

	private TreeConnectingFetcher() {
	}

	@Override
	public <T> Iterable<T> fetch(SelectContext<T> context, Iterable<?> parents) {
		Iterable<T> result = delegateFetcher.fetch(context, parents);

		Iterator<T> childrenIt = result.iterator();
		if (childrenIt.hasNext()) {

			Iterator<?> parentsIt = parents.iterator();
			if (parentsIt.hasNext()) {
				connectToParents(childrenIt, parentsIt);
			}
		}

		return result;
	}

	private <T, P> void connectToParents(Iterator<T> children, Iterator<P> parents) {

		Map<Object, Collection<Object>> childrenByParentId = new HashMap<>();
		while (children.hasNext()) {
			T child = children.next();
			Object key = parentIdMapper.apply(child);
			Collection<Object> parentsChildren = childrenByParentId.computeIfAbsent(key, k -> new ArrayList<Object>());
			parentsChildren.add(child);
		}

		StreamSupport.stream(Spliterators.spliteratorUnknownSize(parents, Spliterator.ORDERED), false)
				.forEach(parent -> {
					Object id = idMapper.apply(parent);
					Collection<Object> parentsChildren = childrenByParentId.get(id);
					parentChildConnector.accept(parent, parentsChildren);
				});
	}

	public static class Builder {

		private TreeConnectingFetcher fetcher = new TreeConnectingFetcher();

		public Fetcher build() {

			Objects.requireNonNull(fetcher.delegateFetcher);
			Objects.requireNonNull(fetcher.idMapper);
			Objects.requireNonNull(fetcher.parentChildConnector);
			Objects.requireNonNull(fetcher.parentIdMapper);

			return fetcher;
		}

		public Builder dataFetcher(Fetcher fetcher) {
			this.fetcher.delegateFetcher = fetcher;
			return this;
		}

		public Builder idMapper(Function<Object, ?> idMapper) {
			fetcher.idMapper = idMapper;
			return this;
		}

		public Builder toManyParentConnector(BiConsumer<Object, Collection<Object>> connector) {
			fetcher.parentChildConnector = connector;
			return this;
		}

		public Builder toOneParentConnector(BiConsumer<Object, Object> connector) {
			fetcher.parentChildConnector = (parent, childCollection) -> {

				Object child;
				if (childCollection.isEmpty()) {
					child = null;
				} else if (childCollection.size() == 1) {
					child = childCollection.iterator().next();
				} else {
					throw new IllegalArgumentException("Expecting single child. Got: " + childCollection.size());
				}

				connector.accept(parent, child);
			};
			return this;
		}

		public Builder parentIdMapper(Function<Object, Object> parentIdMapper) {
			fetcher.parentIdMapper = parentIdMapper;
			return this;
		}

	}

}
