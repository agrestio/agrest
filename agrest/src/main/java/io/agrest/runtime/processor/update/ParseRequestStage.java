package io.agrest.runtime.processor.update;

import io.agrest.AgRequest;
import io.agrest.EntityUpdate;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.protocol.IExcludeParser;
import io.agrest.runtime.protocol.IIncludeParser;
import io.agrest.runtime.protocol.ParameterExtractor;
import org.apache.cayenne.di.Inject;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

    protected static final String PROTOCOL_KEY_EXCLUDE = "exclude";
    protected static final String PROTOCOL_KEY_INCLUDE = "include";

    private IMetadataService metadataService;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;
    private IEntityUpdateParser updateParser;

    public ParseRequestStage(
            @Inject IMetadataService metadataService,
            @Inject IEntityUpdateParser updateParser,
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser) {

        this.updateParser = updateParser;
        this.metadataService = metadataService;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        // Parse response parameters..

        AgRequest request = context.getRequest();

        // TODO: should we skip this for SimpleResponse-returning updates?
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        AgRequest.Builder requestBuilder = AgRequest.builder()
                .includes(getIncludes(request, protocolParameters))
                .excludes(getExcludes(request, protocolParameters));

        context.setRawRequest(requestBuilder.build());

        // Parse updates payload..
        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            AgEntity<T> entity = metadataService.getLrEntity(context.getType());
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }

    private List<Include> getIncludes(AgRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getIncludes().isEmpty() ?
                request.getIncludes() :
                includeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_INCLUDE));
    }

    private List<Exclude> getExcludes(AgRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getExcludes().isEmpty() ?
                request.getExcludes() :
                excludeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_EXCLUDE));
    }
}

