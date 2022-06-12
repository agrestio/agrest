package io.agrest.runtime.entity;

import io.agrest.id.AgObjectId;
import io.agrest.RelatedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.ToManyResourceEntity;
import io.agrest.ToOneResourceEntity;
import io.agrest.access.ReadFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 4.8
 */
public class ResultFilter implements IResultFilter {

    @Override
    public <T> void filterTree(RootResourceEntity<T> entity) {
        ReadFilter<T> filter = entity.getAgEntity().getReadFilter();
        if (!filter.allowsAll() && !entity.getData().isEmpty()) {

            // TODO: if pagination is in effect, we may significantly reduce the number of checks by only applying the
            //  filter to the displayed "window". Not trivial, as we need to filter first to be able to calculate offsets
            //  and limits... Looks like offset will need to be filtered, while limit may be skipped

            // replacing the list to avoid messing up possible data source caches, and also
            // it is likely faster to create a new list than to remove entries from an existing ArrayList
            entity.setData(filterList(entity.getData(), filter));
        }

        filterChildren(entity);
    }

    protected void filterChildren(ResourceEntity<?> entity) {
        for (RelatedResourceEntity<?> child : entity.getChildren().values()) {
            if (child instanceof ToOneResourceEntity) {
                filterToOne((ToOneResourceEntity<?>) child);
            } else {
                filterToMany((ToManyResourceEntity<?>) child);
            }
        }
    }

    protected <T> void filterToOne(ToOneResourceEntity<T> entity) {

        ReadFilter<T> filter = entity.getAgEntity().getReadFilter();
        if (!filter.allowsAll() && !entity.getDataByParent().isEmpty()) {

            // filter the map in place - key removal should be fast
            entity.getDataByParent().entrySet().removeIf(e -> !filter.isAllowed(e.getValue()));
        }

        filterChildren(entity);
    }

    protected <T> void filterToMany(ToManyResourceEntity<T> entity) {

        ReadFilter<T> filter = entity.getAgEntity().getReadFilter();
        if (!filter.allowsAll() && !entity.getDataByParent().isEmpty()) {

            // Filter the map in place;
            // Replace relationship lists to avoid messing up possible data source caches, and also
            // it is likely faster to create a new list than to remove entries from an existing ArrayList
            for (Map.Entry<AgObjectId, List<T>> e : entity.getDataByParent().entrySet()) {
                e.setValue(filterList(e.getValue(), filter));
            }
        }

        filterChildren(entity);
    }

    static <T> List<T> filterList(List<T> unfiltered, ReadFilter<T> filter) {

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

    static <T> List<T> filterListByCopy(List<T> unfiltered, ReadFilter<T> filter, int firstExcluded) {

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
