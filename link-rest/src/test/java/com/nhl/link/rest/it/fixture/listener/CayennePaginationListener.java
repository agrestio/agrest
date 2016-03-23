package com.nhl.link.rest.it.fixture.listener;

import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class CayennePaginationListener {

    public static boolean RESOURCE_ENTITY_IS_FILTERED;
    public static int QUERY_PAGE_SIZE;

    @SelectServerParamsApplied
	public <T> ProcessingStage<SelectContext<T>, T> selectServerParamsApplied(SelectContext<T> context,
                                                                ProcessingStage<SelectContext<T>, T> next) {

		RESOURCE_ENTITY_IS_FILTERED = context.getEntity().isFiltered();
		return next;
	}

    @QueryAssembled
	public <T> ProcessingStage<SelectContext<T>, T> queryAssembled(SelectContext<T> context,
			ProcessingStage<SelectContext<T>, T> next) {

		QUERY_PAGE_SIZE = context.getSelect().getPageSize();
		return next;
	}
}
