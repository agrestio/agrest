package com.nhl.link.rest.sencha;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpConstructor;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByConstructor;
import com.nhl.link.rest.runtime.parser.size.ISizeConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortConstructor;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeConstructor;
import com.nhl.link.rest.runtime.parser.tree.IIncludeConstructor;
import com.nhl.link.rest.runtime.processor.select.ConstructResourceEntityStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Sort;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;

import java.util.List;
import java.util.Map;

public class SenchaConstructResourceEntityStage extends ConstructResourceEntityStage {

    static final String FILTER = "filter";

    private ISortConstructor sortConstructor;
    private ISortParser sortParser;
    private ISenchaFilterProcessor senchaFilterProcessor;

    public SenchaConstructResourceEntityStage(
            @Inject IMetadataService metadataService,
            @Inject ICayenneExpConstructor expConstructor,
            @Inject ISortConstructor sortConstructor,
            @Inject IMapByConstructor mapByConstructor,
            @Inject ISizeConstructor sizeConstructor,
            @Inject IIncludeConstructor includeConstructor,
            @Inject IExcludeConstructor excludeConstructor,
            @Inject ISortParser sortParser,
            @Inject ISenchaFilterProcessor senchaFilterProcessor) {

        super(metadataService, expConstructor, sortConstructor, mapByConstructor,
                sizeConstructor, includeConstructor, excludeConstructor);

        this.sortConstructor = sortConstructor;
        this.sortParser = sortParser;
        this.senchaFilterProcessor = senchaFilterProcessor;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        super.doExecute(context);

        Map<String, List<String>> protocolParameters = context.getProtocolParameters();
        Sort sort = sortParser.fromString(BaseRequestProcessor.string(protocolParameters, Sort.SORT));
        Dir dir = sortParser.dirFromString(BaseRequestProcessor.string(protocolParameters, Dir.DIR));

        ResourceEntity<T>  resourceEntity = context.getEntity();

        sortConstructor.construct(resourceEntity, sort);

        Expression e1 = parseFilter(resourceEntity.getLrEntity(), protocolParameters);
        if (e1 != null) {
            resourceEntity.andQualifier(e1);
        }
    }

    protected Expression parseFilter(LrEntity<?> entity, Map<String, List<String>> protocolParameters) {
        String value = BaseRequestProcessor.string(protocolParameters, FILTER);
        return senchaFilterProcessor.process(entity, value);
    }
}
