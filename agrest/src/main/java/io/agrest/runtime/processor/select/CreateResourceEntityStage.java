package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.protocol.Dir;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.IAgExpMerger;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.meta.IMetadataService;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class CreateResourceEntityStage implements Processor<SelectContext<?, ?>> {

    private IMetadataService metadataService;
    private IAgExpMerger expMerger;
    private ISortMerger sortMerger;
    private IMapByMerger mapByMerger;
    private ISizeMerger sizeMerger;
    private IIncludeMerger includeMerger;
    private IExcludeMerger excludeMerger;

    public CreateResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject IAgExpMerger expMerger,
            @Inject ISortMerger sortMerger,
            @Inject IMapByMerger mapByMerger,
            @Inject ISizeMerger sizeMerger,
            @Inject IIncludeMerger includeMerger,
            @Inject IExcludeMerger excludeMerger) {

        this.metadataService = metadataService;
        this.sortMerger = sortMerger;
        this.expMerger = expMerger;
        this.mapByMerger = mapByMerger;
        this.sizeMerger = sizeMerger;
        this.includeMerger = includeMerger;
        this.excludeMerger = excludeMerger;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?, ?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T, E> void doExecute(SelectContext<T, E> context) {
        ResourceEntity<T, E> resourceEntity = new ResourceEntity<>(metadataService.getAgEntity(context.getType()));

        AgRequest request = context.getRawRequest();
        if (request != null) {
            sizeMerger.merge(resourceEntity, request.getStart(), request.getLimit());
            includeMerger.merge(resourceEntity, request.getIncludes());
            excludeMerger.merge(resourceEntity, request.getExcludes());
            sortMerger.merge(resourceEntity, createSort(context));
            mapByMerger.merge(resourceEntity, request.getMapBy());
            expMerger.merge(resourceEntity, request.getCayenneExp());
        }

        context.setEntity(resourceEntity);
    }

    protected <T, E> Sort createSort(SelectContext<T, E> context) {
        return createSort(context.getRawRequest().getSort(), context.getRawRequest().getSortDirection());
    }

    protected Sort createSort(Sort sort, Dir sortDirection) {

        // ignoring direction on (1) no sort, (2) list sort, (3) no explicit direction
        if (sort == null || sort.getProperty() == null || sortDirection == null) {
            return sort;
        }

        // combine sort property with direction if they were specified separately
        return new Sort(sort.getProperty(), sortDirection);
    }
}
