package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import com.nhl.link.rest.LrRequest;
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
        ResourceEntity<T> resourceEntity
                = new ResourceEntity<>(metadataService.getLrEntity(context.getType()));

        LrRequest request = context.getRawRequest();
        if (request != null) {
            sizeConstructor.construct(resourceEntity, request.getStart(), request.getLimit());
            includeConstructor.construct(resourceEntity, request.getInclude());
            excludeConstructor.construct(resourceEntity, request.getExclude());
            sortConstructor.construct(resourceEntity, request.getSort());
            mapByConstructor.construct(resourceEntity, request.getMapBy());
            expConstructor.construct(resourceEntity, request.getCayenneExp());
        }

        context.setEntity(resourceEntity);
    }
}
