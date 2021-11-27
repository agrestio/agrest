package io.agrest.runtime.processor.select;

import io.agrest.AgObjectId;
import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.filter.ObjectFilter;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        ObjectFilter<T> filter = entity.getAgEntity().getSelectFilter();
        if (!filter.allowsAll() && !entity.getResult().isEmpty()) {

            // replacing the list to avoid messing up possible data source caches, and also
            // it is likely faster to create a new list than to remove entries from an existing ArrayList
            entity.setResult(filterList(entity.getResult(), filter));
        }

        filterChildren(entity);
    }

    protected void filterChildren(ResourceEntity<?> entity) {
        for (NestedResourceEntity<?> child : entity.getChildren().values()) {
            if (child instanceof ToOneResourceEntity) {
                filterToOne((ToOneResourceEntity<?>) child);
            } else {
                filterToMany((ToManyResourceEntity<?>) child);
            }
        }
    }

    protected <T> void filterToOne(ToOneResourceEntity<T> entity) {

        ObjectFilter<T> filter = entity.getAgEntity().getSelectFilter();
        if (!filter.allowsAll() && !entity.getResultsByParent().isEmpty()) {

            // filter the map in place - key removal should be fast
            entity.getResultsByParent().entrySet().removeIf(e -> !filter.isAllowed(e.getValue()));
        }

        filterChildren(entity);
    }

    protected <T> void filterToMany(ToManyResourceEntity<T> entity) {

        ObjectFilter<T> filter = entity.getAgEntity().getSelectFilter();
        if (!filter.allowsAll() && !entity.getResultsByParent().isEmpty()) {

            // Filter the map in place;
            // Replace relationship lists to avoid messing up possible data source caches, and also
            // it is likely faster to create a new list than to remove entries from an existing ArrayList
            for (Map.Entry<AgObjectId, List<T>> e : entity.getResultsByParent().entrySet()) {
                e.setValue(filterList(e.getValue(), filter));
            }
        }

        filterChildren(entity);
    }

    static <T> List<T> filterList(List<T> unfiltered, ObjectFilter<T> filter) {

        int len = unfiltered.size();
        for (int i = 0; i < len; i++) {
            T t = unfiltered.get(i);
            if (!filter.isAllowed(t)) {

                // avoid list copy until we can't
                return filterListByCopy(unfiltered, filter, i);
            }
        }

        return unfiltered;
    }

    static <T> List<T> filterListByCopy(List<T> unfiltered, ObjectFilter<T> filter, int firstExcluded) {

        int len = unfiltered.size();
        List<T> filtered = new ArrayList<>(len - 1);

        for (int i = 0; i < firstExcluded; i++) {
            filtered.add(unfiltered.get(i));
        }

        for (int i = firstExcluded + 1; i < len; i++) {
            T t = unfiltered.get(i);
            if (filter.isAllowed(t)) {
                filtered.add(t);
            }
        }

        return filtered;
    }
}
