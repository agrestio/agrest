package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.IUpdateParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import com.nhl.link.rest.runtime.query.Query;
import org.apache.cayenne.di.Inject;

import java.util.Collection;

/**
 * @since 2.13
 */
public class ConstructResourceEntityStage implements Processor<UpdateContext<?>> {
    private IUpdateParser updateParser;
    private IMetadataService metadataService;
    private IIncludeConstructor includeConstructor;
    private IExcludeConstructor excludeConstructor;


    public ConstructResourceEntityStage(
            @Inject IUpdateParser updateParser,
            @Inject IMetadataService metadataService,
            @Inject IIncludeConstructor includeConstructor,
            @Inject IExcludeConstructor excludeConstructor) {

        this.updateParser = updateParser;
        this.metadataService = metadataService;
        this.includeConstructor = includeConstructor;
        this.excludeConstructor = excludeConstructor;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {
        LrEntity<T> entity = metadataService.getLrEntity(context.getType());
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(entity);

        Query query = context.getRawQuery();
        if (query != null) {
            includeConstructor.construct(resourceEntity, query.getInclude());
            excludeConstructor.construct(resourceEntity, query.getExclude());
        }
        context.setEntity(resourceEntity);

        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }
}
