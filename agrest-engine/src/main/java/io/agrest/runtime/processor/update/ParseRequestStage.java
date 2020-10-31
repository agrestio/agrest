package io.agrest.runtime.processor.update;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.EntityUpdate;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

    protected static final String PROTOCOL_EXCLUDE = "exclude";
    protected static final String PROTOCOL_INCLUDE = "include";

    private AgDataMap dataMap;
    private IEntityUpdateParser updateParser;
    private IAgRequestBuilderFactory requestBuilderFactory;

    public ParseRequestStage(
            @Inject AgDataMap dataMap,
            @Inject IEntityUpdateParser updateParser,
            @Inject IAgRequestBuilderFactory requestBuilderFactory) {

        this.updateParser = updateParser;
        this.dataMap = dataMap;
        this.requestBuilderFactory = requestBuilderFactory;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        // TODO: we should skip this stage for SimpleResponse-returning updates

        context.setMergedRequest(mergedRequest(context));

        // Parse updates payload..
        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            AgEntity<T> entity = dataMap.getEntity(context.getType());
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }

    private AgRequest mergedRequest(UpdateContext<?> context) {

        // TODO: expand to support all protocol keys supported by ParseRequestStage?

        AgRequest request = context.getRequest();
        Map<String, List<String>> parameters = context.getProtocolParameters();

        if (request == null) {
            return requestFromParams(parameters);
        } else if (!parameters.isEmpty()) {
            return new RequestParametersMerger(request, parameters).merge();
        } else {
            return request;
        }
    }

    private AgRequest requestFromParams(Map<String, List<String>> parameters) {
        return requestBuilderFactory.builder()
                .addIncludes(ParameterExtractor.strings(parameters, PROTOCOL_INCLUDE))
                .addExcludes(ParameterExtractor.strings(parameters, PROTOCOL_EXCLUDE))
                .build();
    }

    private class RequestParametersMerger {

        private Map<String, List<String>> parameters;
        private AgRequest request;

        RequestParametersMerger(AgRequest request, Map<String, List<String>> parameters) {
            this.parameters = parameters;
            this.request = request;
        }

        AgRequest merge() {
            AgRequestBuilder builder = requestBuilderFactory.builder();

            setIncludes(builder);
            setExcludes(builder);

            return builder.build();
        }

        private void setIncludes(AgRequestBuilder builder) {
            if (!request.getIncludes().isEmpty()) {
                request.getIncludes().forEach(builder::addInclude);
            } else {
                builder.addIncludes(ParameterExtractor.strings(parameters, PROTOCOL_INCLUDE));
            }
        }

        private void setExcludes(AgRequestBuilder builder) {
            if (!request.getExcludes().isEmpty()) {
                request.getExcludes().forEach(builder::addExclude);
            } else {
                builder.addExcludes(ParameterExtractor.strings(parameters, PROTOCOL_EXCLUDE));
            }
        }
    }
}

