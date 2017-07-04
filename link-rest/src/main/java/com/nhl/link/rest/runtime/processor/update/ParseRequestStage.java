package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor2.Processor;
import com.nhl.link.rest.processor2.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IRequestParser;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import org.apache.cayenne.di.Inject;

import java.util.Collection;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

    private IRequestParser requestParser;
    private IUpdateParser updateParser;
    private IMetadataService metadataService;

    public ParseRequestStage(
            @Inject IRequestParser requestParser,
            @Inject IUpdateParser updateParser,
            @Inject IMetadataService metadataService) {

        this.requestParser = requestParser;
        this.updateParser = updateParser;
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        LrEntity<T> entity = metadataService.getLrEntity(context.getType());

        // TODO: should we skip this for SimpleResponse-returning updates?
        ResourceEntity<T> resourceEntity = requestParser.parseUpdate(entity, context.getUriInfo());
        context.setEntity(resourceEntity);

        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }
}
