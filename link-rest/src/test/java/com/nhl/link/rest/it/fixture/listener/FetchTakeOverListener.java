package com.nhl.link.rest.it.fixture.listener;

import java.util.ArrayList;
import java.util.List;

import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class FetchTakeOverListener {

	public static boolean BEFORE_FETCH_CALLED;

	@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
	@SelectServerParamsApplied
	public <T> ProcessingStage<SelectContext<T>, T> beforeFetch(SelectContext<T> context,
			ProcessingStage<SelectContext<T>, T> next) {

		BEFORE_FETCH_CALLED = true;

		List objects = new ArrayList<>();
		objects.add(new E3() {
			{
				setName("__X__");
			}
		});
		objects.add(new E3() {
			{
				setName("__Y__");
			}
		});
		context.setObjects(objects);

		// we handle the data, so block the rest of the chain
		return null;
	}
}
