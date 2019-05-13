package io.agrest.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.ResourceEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.entity.ICayenneExpMerger;
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
        ResourceEntity<T> resourceEntity = new ResourceEntity<>(metadataService.getAgEntity(context.getType()));

        // TODO: no reason why extra properties can't be hierarchical, i.e. we need to parse dot-path here and assign
        //  children to child ResourceEntities
        if (context.getExtraProperties() != null) {
            resourceEntity.getExtraProperties().putAll(context.getExtraProperties());
        }

        AgRequest request = context.getRawRequest();
        if (request != null) {
            sizeMerger.merge(resourceEntity, request.getStart(), request.getLimit());
            includeMerger.merge(resourceEntity, request.getIncludes());
            excludeMerger.merge(resourceEntity, request.getExcludes());
            sortMerger.merge(resourceEntity, request.getSort());
            mapByMerger.merge(resourceEntity, request.getMapBy());
            expMerger.merge(resourceEntity, request.getCayenneExp());
        }

        context.setEntity(resourceEntity);
    }
}
