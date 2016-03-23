package com.nhl.link.rest.it.fixture.listener;

import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

public class UpdateCallbackListener {

	public static boolean BEFORE_UPDATE_CALLED;

	@UpdateServerParamsApplied
	public void beforeFetch(UpdateContext<?> context) {
		BEFORE_UPDATE_CALLED = true;
	}
}
