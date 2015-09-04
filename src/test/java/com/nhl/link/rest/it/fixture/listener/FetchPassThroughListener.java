package com.nhl.link.rest.it.fixture.listener;

import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class FetchPassThroughListener {

	public static boolean BEFORE_FETCH_CALLED;

	@SelectServerParamsApplied
	public <T> BaseLinearProcessingStage<SelectContext<T>, T> beforeFetch(SelectContext<T> context,
			BaseLinearProcessingStage<SelectContext<T>, T> next) {

		BEFORE_FETCH_CALLED = true;
		return next;
	}
}
