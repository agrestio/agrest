package io.agrest.runtime.processor.select;

import io.agrest.NestedResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.filter.ObjectFilter;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 4.8
 */
public class FilterDataStage implements Processor<SelectContext<?>> {

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        filterRoot(context.getEntity());
    }

    protected <T> void filterRoot(RootResourceEntity<T> entity) {
        ObjectFilter<T> objectFilter = entity.getAgEntity().getReadableObjectFilter();
        if (!objectFilter.allowAll() && !entity.getResult().isEmpty()) {
            entity.setResult(filter(entity.getResult(), objectFilter));
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            filterNested(child);
        }
    }

    protected void filterNested(NestedResourceEntity<?> entity) {

        ObjectFilter<?> objectFilter = entity.getAgEntity().getReadableObjectFilter();
        if (!objectFilter.allowAll() && !entity.getResultsByParent().isEmpty()) {
            // TODO
        }

        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            filterNested(child);
        }
    }

    private <T> List<T> filter(List<T> unfiltered, ObjectFilter<T> filter) {

        int len = unfiltered.size();
        for (int i = 0; i < len; i++) {
            T t = unfiltered.get(i);
            if (!filter.isAccessible(t)) {

                // avoid list copy until we can't
                return filterByCopy(unfiltered, filter, i);
            }
        }

        return unfiltered;
    }

    private <T> List<T> filterByCopy(List<T> unfiltered, ObjectFilter<T> filter, int firstExcluded) {

        int len = unfiltered.size();
        List<T> filtered = new ArrayList<>(len - 1);

        for (int i = 0; i < firstExcluded; i++) {
            filtered.add(unfiltered.get(i));
        }

        for (int i = firstExcluded + 1; i < len; i++) {
            T t = unfiltered.get(i);
            if (filter.isAccessible(t)) {
                filtered.add(t);
            }
        }

        return filtered;
    }
}
