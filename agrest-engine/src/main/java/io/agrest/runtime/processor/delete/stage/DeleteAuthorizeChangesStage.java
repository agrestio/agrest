package io.agrest.runtime.processor.delete.stage;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.IChangeAuthorizer;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.di.Inject;

/**
 * A processor associated with {@link io.agrest.DeleteStage#AUTHORIZE_CHANGES} that runs delete authorization filters
 * against request operations. It would fail the chain if at least one rule is not satisfied.
 *
 * @since 5.0
 */
public class DeleteAuthorizeChangesStage implements Processor<DeleteContext<?>> {

    private final IChangeAuthorizer changeAuthorizer;

    public DeleteAuthorizeChangesStage(@Inject IChangeAuthorizer changeAuthorizer) {
        this.changeAuthorizer = changeAuthorizer;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(DeleteContext<T> context) {
        changeAuthorizer.checkDelete(context.getDeleteOperations(), context.getAgEntity().getDeleteAuthorizer());
    }
}
