package io.agrest.runtime.processor.update.stage;

import io.agrest.EntityUpdate;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.update.UpdateContext;
import io.agrest.runtime.protocol.IEntityUpdateParser;
import org.apache.cayenne.di.Inject;

import java.util.Collection;

/**
 * @since 5.0
 */
public class UpdateParseRequestStage implements Processor<UpdateContext<?>> {

    private AgDataMap dataMap;
    private IEntityUpdateParser updateParser;

    public UpdateParseRequestStage(@Inject AgDataMap dataMap, @Inject IEntityUpdateParser updateParser) {
        this.updateParser = updateParser;
        this.dataMap = dataMap;
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

