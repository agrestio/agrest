package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.annotation.listener.UpdateResponseUpdated;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * Organizes listener annotations by the type of LinkRest chain they participate
 * in.
 *
 * @since 1.19
 * @deprecated since 2.7 as annotated listeners were deprecated in favor of the functional interceptor API.
 */
public enum EventGroup {

    select(SelectChainInitialized.class, SelectRequestParsed.class, SelectServerParamsApplied.class, QueryAssembled.class,
            DataFetched.class),

    update(UpdateChainInitialized.class, UpdateRequestParsed.class, UpdateServerParamsApplied.class, DataStoreUpdated.class,
            UpdateResponseUpdated.class);

    private final Collection<Class<? extends Annotation>> eventsFired;

    @SafeVarargs
    private EventGroup(Class<? extends Annotation>... eventsFired) {
        this.eventsFired = Arrays.asList(eventsFired);
    }

    public Collection<Class<? extends Annotation>> getEventsFired() {
        return eventsFired;
    }
}
