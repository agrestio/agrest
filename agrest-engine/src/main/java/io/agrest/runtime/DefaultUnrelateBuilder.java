package io.agrest.runtime;

import io.agrest.id.AgObjectId;
import io.agrest.SimpleResponse;
import io.agrest.UnrelateBuilder;
import io.agrest.runtime.processor.unrelate.UnrelateContext;
import io.agrest.runtime.processor.unrelate.UnrelateProcessorFactory;

import java.util.Map;

/**
 * @since 5.0
 */
public class DefaultUnrelateBuilder<T> implements UnrelateBuilder<T> {

    private final UnrelateContext<T> context;
    private final UnrelateProcessorFactory processorFactory;

    public DefaultUnrelateBuilder(UnrelateContext<T> context, UnrelateProcessorFactory processorFactory) {
        this.context = context;
        this.processorFactory = processorFactory;
    }

    @Override
    public UnrelateBuilder<T> sourceId(Object id) {
        context.setSourceId(AgObjectId.of(id));
        return this;
    }

    @Override
    public UnrelateBuilder<T> sourceId(Map<String, Object> ids) {
        context.setSourceId(AgObjectId.ofMap(ids));
        return this;
    }

    @Override
    public UnrelateBuilder<T> allRelated(String relationship) {
        context.setRelationship(relationship);
        context.setTargetId(null);
        return this;
    }

    @Override
    public UnrelateBuilder<T> related(String relationship, Object targetId) {
        context.setRelationship(relationship);
        context.setTargetId(AgObjectId.of(targetId));
        return this;
    }

    @Override
    public UnrelateBuilder<T> related(String relationship, Map<String, Object> targetId) {
        context.setRelationship(relationship);
        context.setTargetId(AgObjectId.ofMap(targetId));
        return this;
    }

    @Override
    public SimpleResponse sync() {
        processorFactory.createProcessor().execute(context);
        return SimpleResponse.of(context.getStatus());
    }
}
