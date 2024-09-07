package io.agrest.cayenne.processor.delete.stage;

import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.delete.stage.DeleteInDataStoreStage;
import io.agrest.runtime.processor.update.ChangeOperation;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 5.0
 */
public class CayenneDeleteInDataStoreStage extends DeleteInDataStoreStage {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        doExecute((DeleteContext<Persistent>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends Persistent> void doExecute(DeleteContext<T> context) {

        List<T> objects = new ArrayList<>(context.getDeleteOperations().size());
        for (ChangeOperation<T> op : context.getDeleteOperations()) {
            objects.add(op.getObject());
        }

        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);

        cayenneContext.deleteObjects(objects);
        cayenneContext.commitChanges();
    }
}
