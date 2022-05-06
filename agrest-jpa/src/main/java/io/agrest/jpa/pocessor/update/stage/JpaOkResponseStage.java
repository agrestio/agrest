package io.agrest.jpa.pocessor.update.stage;

import io.agrest.HttpStatus;
import io.agrest.jpa.persister.IAgJpaPersister;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.di.Inject;

/**
 * @since 5.0
 */
public class JpaOkResponseStage extends JpaFillResponseStage {

    public JpaOkResponseStage(@Inject IAgJpaPersister persister) {
        super(persister);
    }

    @Override
    protected int getHttpStatus(UpdateContext<Object> context) {
        return HttpStatus.OK;
    }
}