package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.parser.filter.ISenchaFilterParser;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

public class SenchaParseRequestStage extends ParseRequestStage {

    static final String FILTER = "filter";
    static final String GROUP = "group";
    static final String GROUP_DIR = "groupDir";

    private ISortParser sortParser;
    private ISenchaFilterParser filterParser;

    public SenchaParseRequestStage(
            @Inject ICayenneExpParser expParser,
            @Inject ISortParser sortParser,
            @Inject IMapByParser mapByParser,
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser,
            @Inject ISenchaFilterParser filterParser) {

        super(expParser, sortParser, mapByParser, includeParser, excludeParser);
        this.sortParser = sortParser;
        this.filterParser = filterParser;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {

        super.doExecute(context);

        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        SenchaRequest.Builder builder = SenchaRequest.builder()
                .group(sortParser.fromString(BaseRequestProcessor.string(protocolParameters, GROUP)))
                .groupDirection(sortParser.dirFromString(BaseRequestProcessor.string(protocolParameters, GROUP_DIR)))
                .filters(filterParser.fromString(BaseRequestProcessor.string(protocolParameters, FILTER)));

        SenchaRequest.set(context, builder.build());
    }
}
