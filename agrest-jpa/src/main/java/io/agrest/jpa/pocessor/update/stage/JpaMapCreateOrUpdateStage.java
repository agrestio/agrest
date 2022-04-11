package io.agrest.jpa.pocessor.update.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.agrest.EntityUpdate;
import io.agrest.jpa.exp.IJpaExpParser;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.jpa.pocessor.IJpaQueryAssembler;
import io.agrest.runtime.processor.update.ChangeOperation;
import io.agrest.runtime.processor.update.ChangeOperationType;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaMapCreateOrUpdateStage extends JpaMapUpdateStage {

    public JpaMapCreateOrUpdateStage(
            @Inject IJpaExpParser qualifierParser,
            @Inject IJpaQueryAssembler queryAssembler,
            @Inject IAgJpaPersister persister) {
        super(qualifierParser, queryAssembler, persister);
    }

    @Override
    protected <T> void collectCreateOps(
            UpdateContext<T> context,
            UpdateMap<T> updateMap) {

        List<EntityUpdate<T>> noKeyCreate = updateMap.getNoId();
        Collection<EntityUpdate<T>> withKeyCreate = updateMap.getWithId();

        List<ChangeOperation<T>> createOps = new ArrayList<>(noKeyCreate.size() + withKeyCreate.size());

        for (EntityUpdate<T> u : noKeyCreate) {
            createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, u.getEntity(), null, u));
        }

        for (EntityUpdate<T> u : withKeyCreate) {
            createOps.add(new ChangeOperation<>(ChangeOperationType.CREATE, u.getEntity(), null, u));
        }

        context.setChangeOperations(ChangeOperationType.CREATE, createOps);
    }
}
