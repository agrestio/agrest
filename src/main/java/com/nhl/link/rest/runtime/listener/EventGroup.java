package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

import com.nhl.link.rest.annotation.SelectServerParamsApplied;
import com.nhl.link.rest.annotation.Fetched;
import com.nhl.link.rest.annotation.SelectRequestParsed;
import com.nhl.link.rest.annotation.SelectChainInitialized;

/**
 * Organizes listener annotations by the type of LinKRest they participate in.
 * 
 * @since 1.19
 */
public enum EventGroup {

	select(SelectServerParamsApplied.class, Fetched.class, SelectRequestParsed.class,
			SelectChainInitialized.class);

	private final Collection<Class<? extends Annotation>> eventsFired;

	@SafeVarargs
	private EventGroup(Class<? extends Annotation>... eventsFired) {
		this.eventsFired = Arrays.asList(eventsFired);
	}

	public Collection<Class<? extends Annotation>> getEventsFired() {
		return eventsFired;
	}
}
