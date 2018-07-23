package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.entity.ICayenneExpMerger;
import com.nhl.link.rest.runtime.entity.IMapByMerger;
import com.nhl.link.rest.runtime.entity.ISizeMerger;
import com.nhl.link.rest.runtime.entity.ISortMerger;
import com.nhl.link.rest.runtime.entity.IExcludeMerger;
import com.nhl.link.rest.runtime.entity.IIncludeMerger;
import com.nhl.link.rest.runtime.processor.select.CreateResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import static java.util.Arrays.asList;

public class SenchaConstructResourceEntityStage extends CreateResourceEntityStage {


    private ISenchaFilterConstructor senchaFilterProcessor;

    public SenchaConstructResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject ICayenneExpMerger expConstructor,
            @Inject ISortMerger sortConstructor,
            @Inject IMapByMerger mapByConstructor,
            @Inject ISizeMerger sizeConstructor,
            @Inject IIncludeMerger includeConstructor,
            @Inject IExcludeMerger excludeConstructor,
            @Inject ISenchaFilterConstructor senchaFilterProcessor) {

        super(metadataService, expConstructor, sortConstructor, mapByConstructor,
                sizeConstructor, includeConstructor, excludeConstructor);

        this.senchaFilterProcessor = senchaFilterProcessor;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);

        ResourceEntity<T> resourceEntity = context.getEntity();

        Expression e1 = parseFilter(resourceEntity.getLrEntity(), context);
        if (e1 != null) {
            resourceEntity.andQualifier(e1);
        }
    }

    protected <T> Expression parseFilter(LrEntity<?> entity, SelectContext<T> context) {
        SenchaRequest senchaRequest = SenchaRequest.get(context);
        return senchaFilterProcessor.process(entity, senchaRequest.getFilters());
    }

    @Override
    protected <T> Sort createSort(SelectContext<T> context) {
        Sort sort = super.createSort(context);
        Sort groupSort = createGroupSort(context);

        if (sort == null) {
            return groupSort;
        }

        if (groupSort == null) {
            return sort;
        }

        // merge group and sort; group goes first
        return new Sort(asList(groupSort, sort));
    }

    protected <T> Sort createGroupSort(SelectContext<T> context) {
        SenchaRequest senchaRequest = SenchaRequest.get(context);
        return createSort(senchaRequest.getGroup(), senchaRequest.getGroupDirection());
    }
}
