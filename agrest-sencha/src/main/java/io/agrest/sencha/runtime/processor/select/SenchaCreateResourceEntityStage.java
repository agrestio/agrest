package io.agrest.sencha.runtime.processor.select;

import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.protocol.Sort;
import io.agrest.runtime.entity.IAgExpMerger;
import io.agrest.runtime.entity.IExcludeMerger;
import io.agrest.runtime.entity.IIncludeMerger;
import io.agrest.runtime.entity.IMapByMerger;
import io.agrest.runtime.entity.ISizeMerger;
import io.agrest.runtime.entity.ISortMerger;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.runtime.entity.ISenchaFilterExpressionCompiler;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import static java.util.Arrays.asList;

public class SenchaCreateResourceEntityStage extends CreateResourceEntityStage {


    private ISenchaFilterExpressionCompiler<Expression> senchaFilterProcessor;

    public SenchaCreateResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject IAgExpMerger expConstructor,
            @Inject ISortMerger sortConstructor,
            @Inject IMapByMerger mapByConstructor,
            @Inject ISizeMerger sizeConstructor,
            @Inject IIncludeMerger includeConstructor,
            @Inject IExcludeMerger excludeConstructor,
            @Inject ISenchaFilterExpressionCompiler senchaFilterProcessor) {

        super(metadataService, expConstructor, sortConstructor, mapByConstructor,
                sizeConstructor, includeConstructor, excludeConstructor);

        this.senchaFilterProcessor = senchaFilterProcessor;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?, ?> context) {
        doSenchaExecute((SelectContext<DataObject, Expression>)context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void doSenchaExecute(SelectContext<T, Expression> context) {
        super.doExecute(context);

        ResourceEntity<T, Expression> resourceEntity = context.getEntity();

        Expression e1 = parseFilter(resourceEntity.getAgEntity(), context);
        if (e1 != null) {
            resourceEntity.andQualifier(e1);
        }
    }

    protected <T extends DataObject> Expression parseFilter(AgEntity<?> entity, SelectContext<T, Expression> context) {
        SenchaRequest senchaRequest = SenchaRequest.get(context);
        return senchaFilterProcessor.process(entity, senchaRequest.getFilters());
    }

    @Override
    protected <T, E> Sort createSort(SelectContext<T, E> context) {
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

    protected <T, E> Sort createGroupSort(SelectContext<T, E> context) {
        SenchaRequest senchaRequest = SenchaRequest.get(context);
        return createSort(senchaRequest.getGroup(), senchaRequest.getGroupDirection());
    }
}
