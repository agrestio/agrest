package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<SelectContext<?>> {

    private IRequestParser requestParser;
    private IMetadataService metadataService;

    public ParseRequestStage(
            @Inject IRequestParser requestParser,
            @Inject IMetadataService metadataService) {

        this.requestParser = requestParser;
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        LrEntity<T> entity = metadataService.getLrEntity(context.getType());
        ResourceEntity<T> resourceEntity = requestParser.parseSelect(entity, context.getProtocolParameters(), context.getQuery());
        context.setEntity(resourceEntity);
    }
}