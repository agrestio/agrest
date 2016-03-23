package com.nhl.link.rest.it.fixture.listener;

import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class FetchCallbackListener {
	
	public static boolean BEFORE_FETCH_CALLED;

	@SelectServerParamsApplied
	public void beforeFetch(SelectContext<?> context) {
		BEFORE_FETCH_CALLED = true;
	}
}
