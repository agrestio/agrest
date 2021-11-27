package io.agrest.runtime.processor.update;

import io.agrest.filter.CreateFilter;
import io.agrest.filter.DeleteFilter;
import io.agrest.filter.UpdateFilter;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * @since 4.8
 */
public class FilterChangesStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        CreateFilter<T> createFilter = context.getEntity().getAgEntity().getCreateFilter();
        if (!createFilter.allowsAll()) {

            List<ChangeOperation<T>> ops = context.getChangeOperations().get(ChangeOperationType.CREATE);
            if (!ops.isEmpty()) {
                context.setChangeOperations(
                        ChangeOperationType.CREATE,
                        filterList(ops, op -> createFilter.isAllowed(op.getUpdate())));
            }
        }

        UpdateFilter<T> updateFilter = context.getEntity().getAgEntity().getUpdateFilter();
        if (!updateFilter.allowsAll()) {

            List<ChangeOperation<T>> ops = context.getChangeOperations().get(ChangeOperationType.UPDATE);
            if (!ops.isEmpty()) {
                context.setChangeOperations(
                        ChangeOperationType.UPDATE,
                        filterList(ops, op -> updateFilter.isAllowed(op.getObject(), op.getUpdate())));
            }
        }

        DeleteFilter<T> deleteFilter = context.getEntity().getAgEntity().getDeleteFilter();
        if (!deleteFilter.allowsAll()) {

            List<ChangeOperation<T>> ops = context.getChangeOperations().get(ChangeOperationType.DELETE);
            if (!ops.isEmpty()) {
                context.setChangeOperations(
                        ChangeOperationType.DELETE,
                        filterList(ops, op -> deleteFilter.isAllowed(op.getObject())));
            }
        }
    }

    static <T> List<ChangeOperation<T>> filterList(
            List<ChangeOperation<T>> unfiltered,
            Predicate<ChangeOperation<T>> filter) {

        int len = unfiltered.size();
        for (int i = 0; i < len; i++) {
            ChangeOperation<T> op = unfiltered.get(i);
            if (!filter.test(op)) {

                // avoid list copy until we can't
                return filterListByCopy(unfiltered, filter, i);
            }
        }

        return unfiltered;
    }

    static <T> List<ChangeOperation<T>> filterListByCopy(
            List<ChangeOperation<T>> unfiltered,
            Predicate<ChangeOperation<T>> filter,
            int firstExcluded) {

        int len = unfiltered.size();
        List<ChangeOperation<T>> filtered = new ArrayList<>(len - 1);

        for (int i = 0; i < firstExcluded; i++) {
            filtered.add(unfiltered.get(i));
        }

        for (int i = firstExcluded + 1; i < len; i++) {
            ChangeOperation<T> op = unfiltered.get(i);
            if (filter.test(op)) {
                filtered.add(op);
            }
        }

        return filtered;
    }
}
