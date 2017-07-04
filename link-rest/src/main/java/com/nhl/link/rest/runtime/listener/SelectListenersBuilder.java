package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.QueryAssembled;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.annotation.listener.SelectServerParamsApplied;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @since 2.7
 * @deprecated since 2.7 . Exists to support deprecated listeners.
 */
public class SelectListenersBuilder extends ListenersBuilder<SelectStage> {

    private static final Map<Class<? extends Annotation>, SelectStage> ANNOTATIONS_MAP;

    static {
        ANNOTATIONS_MAP = new HashMap<>();
        ANNOTATIONS_MAP.put(SelectChainInitialized.class, SelectStage.START);
        ANNOTATIONS_MAP.put(SelectRequestParsed.class, SelectStage.PARSE_REQUEST);
        ANNOTATIONS_MAP.put(SelectServerParamsApplied.class, SelectStage.APPLY_SERVER_PARAMS);
        ANNOTATIONS_MAP.put(QueryAssembled.class, SelectStage.ASSEMBLE_QUERY);
        ANNOTATIONS_MAP.put(DataFetched.class, SelectStage.FETCH_DATA);
    }

    private SelectBuilder<?> builder;


    public SelectListenersBuilder(SelectBuilder<?> builder, IListenerService listenerService, SelectContext<?> context) {
        super(listenerService, context, EventGroup.select);
        this.builder = builder;
    }

    @Override
    protected SelectStage mapStage(Class<? extends Annotation> annotation) {
        return Objects.requireNonNull(ANNOTATIONS_MAP.get(annotation));
    }

    @Override
    protected void appendInvocation(SelectStage stage, ListenerInvocation invocation) {
        builder.routingStage(stage, invocation::invoke);
    }
}
