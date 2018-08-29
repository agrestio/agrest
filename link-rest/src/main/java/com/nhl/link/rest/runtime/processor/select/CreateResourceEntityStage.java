package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.entity.ICayenneExpMerger;
import com.nhl.link.rest.runtime.entity.IMapByMerger;
import com.nhl.link.rest.runtime.entity.ISizeMerger;
import com.nhl.link.rest.runtime.entity.ISortMerger;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class CreateResourceEntityStage implements Processor<SelectContext<?>> {

    private IMetadataService metadataService;
    private ICayenneExpMerger expMerger;
    private ISortMerger sortMerger;
    private IMapByMerger mapByMerger;
    private ISizeMerger sizeMerger;
    private IIncludeMerger includeMerger;
    private IExcludeMerger excludeMerger;

    public CreateResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject ICayenneExpMerger expMerger,
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
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(metadataService.getLrEntity(context.getType()));

        LrRequest request = context.getRawRequest();
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

    protected <T> Sort createSort(SelectContext<T> context) {
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
