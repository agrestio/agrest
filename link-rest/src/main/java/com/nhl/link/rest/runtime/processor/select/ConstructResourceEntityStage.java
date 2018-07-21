package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.13
 */
public class ConstructResourceEntityStage implements Processor<SelectContext<?>> {

    private IMetadataService metadataService;
    private ICayenneExpConstructor expConstructor;
    private ISortConstructor sortConstructor;
    private IMapByConstructor mapByConstructor;
    private ISizeConstructor sizeConstructor;
    private IIncludeConstructor includeConstructor;
    private IExcludeConstructor excludeConstructor;

    public ConstructResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject ICayenneExpConstructor expConstructor,
            @Inject ISortConstructor sortConstructor,
            @Inject IMapByConstructor mapByConstructor,
            @Inject ISizeConstructor sizeConstructor,
            @Inject IIncludeConstructor includeConstructor,
            @Inject IExcludeConstructor excludeConstructor) {

        this.metadataService = metadataService;
        this.sortConstructor = sortConstructor;
        this.expConstructor = expConstructor;
        this.mapByConstructor = mapByConstructor;
        this.sizeConstructor = sizeConstructor;
        this.includeConstructor = includeConstructor;
        this.excludeConstructor = excludeConstructor;
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
            sizeConstructor.construct(resourceEntity, request.getStart(), request.getLimit());
            includeConstructor.construct(resourceEntity, request.getIncludes());
            excludeConstructor.construct(resourceEntity, request.getExcludes());
            sortConstructor.construct(resourceEntity, createSort(context));
            mapByConstructor.construct(resourceEntity, request.getMapBy());
            expConstructor.construct(resourceEntity, request.getCayenneExp());
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
