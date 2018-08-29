package com.nhl.link.rest.sencha.runtime.processor.select;

import com.nhl.link.rest.runtime.protocol.ParameterExtractor;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.sencha.SenchaRequest;
import com.nhl.link.rest.sencha.runtime.protocol.ISenchaFilterParser;
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
                .group(sortParser.fromString(ParameterExtractor.string(protocolParameters, GROUP)))
                .groupDirection(sortParser.dirFromString(ParameterExtractor.string(protocolParameters, GROUP_DIR)))
                .filters(filterParser.fromString(ParameterExtractor.string(protocolParameters, FILTER)));

        SenchaRequest.set(context, builder.build());
    }
}
