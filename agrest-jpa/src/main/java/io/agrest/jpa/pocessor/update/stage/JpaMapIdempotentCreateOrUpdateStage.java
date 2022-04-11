package io.agrest.jpa.pocessor.update.stage;

import io.agrest.AgException;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaMapIdempotentCreateOrUpdateStage extends JpaMapCreateOrUpdateStage {

    public JpaMapIdempotentCreateOrUpdateStage(
            @Inject IJpaExpParser qualifierParser,
            @Inject IJpaQueryAssembler queryAssembler,
            @Inject IAgJpaPersister persister) {
        super(qualifierParser, queryAssembler, persister);
    }

    @Override
    protected <T> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        if (!updateMap.getNoId().isEmpty()) {
            throw AgException.badRequest("Request is not idempotent.");
        }

        super.collectCreateOps(context, updateMap);
    }
}
