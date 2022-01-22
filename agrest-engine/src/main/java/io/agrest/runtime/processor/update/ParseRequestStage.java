package io.agrest.runtime.processor.update;

import io.agrest.EntityUpdate;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import org.apache.cayenne.di.Inject;

import java.util.Collection;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

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

        // Parse updates payload..
        // skip parsing if we already received EntityUpdates collection parsed by MessageBodyReader
        if (context.getUpdates() == null) {
            AgEntity<T> entity = dataMap.getEntity(context.getType());
            Collection<EntityUpdate<T>> updates = updateParser.parse(entity, context.getEntityData());
            context.setUpdates(updates);
        }
    }
}

