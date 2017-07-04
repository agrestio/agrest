package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.UpdateBuilder;
import com.nhl.link.rest.UpdateStage;
import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.annotation.listener.UpdateResponseUpdated;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @since 2.7
 * @deprecated since 2.7 . Exists to support deprecated listeners.
 */
public class UpdateListenersBuilder extends ListenersBuilder<UpdateStage> {

    private static final Map<Class<? extends Annotation>, UpdateStage> ANNOTATIONS_MAP;

    static {
        ANNOTATIONS_MAP = new HashMap<>();
        ANNOTATIONS_MAP.put(UpdateChainInitialized.class, UpdateStage.START);
        ANNOTATIONS_MAP.put(UpdateRequestParsed.class, UpdateStage.PARSE_REQUEST);
        ANNOTATIONS_MAP.put(UpdateServerParamsApplied.class, UpdateStage.APPLY_SERVER_PARAMS);
        ANNOTATIONS_MAP.put(DataStoreUpdated.class, UpdateStage.UPDATE_DATA_STORE);
        ANNOTATIONS_MAP.put(UpdateResponseUpdated.class, UpdateStage.FILL_RESPONSE);
    }

    private UpdateBuilder<?> builder;

    public UpdateListenersBuilder(UpdateBuilder<?> builder, IListenerService listenerService, UpdateContext<?> context) {
        super(listenerService, context, EventGroup.update);
        this.builder = builder;
    }

    @Override
    protected UpdateStage mapStage(Class<? extends Annotation> annotation) {
        return Objects.requireNonNull(ANNOTATIONS_MAP.get(annotation));
    }

    @Override
    protected void appendInvocation(UpdateStage stage, ListenerInvocation invocation) {
        builder.routingStage(stage, invocation::invoke);
    }
}
