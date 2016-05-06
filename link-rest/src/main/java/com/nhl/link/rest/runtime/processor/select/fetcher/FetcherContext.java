package com.nhl.link.rest.runtime.processor.select.fetcher;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

/**
 * @since 2.0
 */
public class FetcherContext<T> {
	
	@SuppressWarnings("rawtypes")
	private static final FetcherContext ROOT_CONTEXT = new FetcherContext<Object>(null, null, null);
	
	@SuppressWarnings("unchecked")
	public static <T> FetcherContext<T> getRootContext(Class<T> rootType) {
		return ROOT_CONTEXT;
	}

	private SelectContext<T> selectContext;
	private LrEntity<?> parentEntity;
	private LrRelationship relationshipFromParent;

	public FetcherContext(SelectContext<T> selectContext, LrEntity<?> parentEntity,
			LrRelationship relationshipFromParent) {
		this.selectContext = selectContext;
		this.parentEntity = parentEntity;
		this.relationshipFromParent = relationshipFromParent;
	}

	public SelectContext<T> getSelectContext() {
		return selectContext;
	}

	public LrEntity<?> getParentEntity() {
		return parentEntity;
	}

	public LrRelationship getRelationshipFromParent() {
		return relationshipFromParent;
	}
}
