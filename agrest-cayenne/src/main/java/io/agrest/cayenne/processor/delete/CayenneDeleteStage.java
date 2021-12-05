package io.agrest.cayenne.processor.delete;

import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.delete.DeleteContext;
import io.agrest.runtime.processor.update.ChangeOperation;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 2.7
 */
public class CayenneDeleteStage implements Processor<DeleteContext<?>> {

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        doExecute((DeleteContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void doExecute(DeleteContext<T> context) {

        List<T> objects = new ArrayList<>(context.getDeleteOperations().size());
        for (ChangeOperation<T> op : context.getDeleteOperations()) {
            objects.add(op.getObject());
        }

        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);

        cayenneContext.deleteObjects(objects);
        cayenneContext.commitChanges();
    }
}
