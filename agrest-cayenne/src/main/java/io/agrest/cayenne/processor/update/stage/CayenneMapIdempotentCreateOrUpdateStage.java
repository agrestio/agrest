package io.agrest.cayenne.processor.update.stage;

import io.agrest.AgException;
import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.cayenne.processor.ICayenneQueryAssembler;
import io.agrest.cayenne.exp.ICayenneExpParser;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.Persistent;
import org.apache.cayenne.di.Inject;

/**
 * @since 4.8
 */
public class CayenneMapIdempotentCreateOrUpdateStage extends CayenneMapCreateOrUpdateStage {

    public CayenneMapIdempotentCreateOrUpdateStage(
            @Inject ICayenneExpParser qualifierParser,
            @Inject ICayenneQueryAssembler queryAssembler,
            @Inject ICayennePersister persister) {
        super(qualifierParser, queryAssembler, persister);
    }

    @Override
    protected <T extends Persistent> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        if (!updateMap.getNoId().isEmpty()) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.collectCreateOps(context, updateMap);
    }
}
