package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.protocol.IEntityUpdateParser;
import com.nhl.link.rest.runtime.protocol.ParameterExtractor;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
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

        LrRequest request = context.getRequest();

        // TODO: should we skip this for SimpleResponse-returning updates?
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        LrRequest.Builder requestBuilder = LrRequest.builder()
                .includes(getIncludes(request, protocolParameters))
                .excludes(getExcludes(request, protocolParameters));

        context.setRawRequest(requestBuilder.build());

        // Parse updates payload..
        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            LrEntity<T> entity = metadataService.getLrEntity(context.getType());
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }

    private List<Include> getIncludes(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getIncludes().isEmpty() ?
                request.getIncludes() :
                includeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_INCLUDE));
    }

    private List<Exclude> getExcludes(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getExcludes().isEmpty() ?
                request.getExcludes() :
                excludeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_EXCLUDE));
    }
}

