package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.protocol.Start;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<SelectContext<?>> {

    private ICayenneExpParser expParser;
    private ISortParser sortParser;
    private IMapByParser mapByParser;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public ParseRequestStage(
            @Inject ICayenneExpParser expParser,
            @Inject ISortParser sortParser,
            @Inject IMapByParser mapByParser,
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser) {

        this.expParser = expParser;
        this.sortParser = sortParser;
        this.mapByParser = mapByParser;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        LrRequest request = new LrRequest(
                expParser.fromString(BaseRequestProcessor.string(protocolParameters, CayenneExp.CAYENNE_EXP)),
                sortParser.fromString(BaseRequestProcessor.string(protocolParameters, Sort.SORT),
                        BaseRequestProcessor.string(protocolParameters, Dir.DIR)),
                sortParser.dirFromString(BaseRequestProcessor.string(protocolParameters, Dir.DIR)),
                mapByParser.fromString(BaseRequestProcessor.string(protocolParameters, MapBy.MAP_BY)),
                new Start(BaseRequestProcessor.integer(protocolParameters, Start.START)),
                new Limit(BaseRequestProcessor.integer(protocolParameters, Limit.LIMIT)),
                includeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Include.INCLUDE)),
                excludeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Exclude.EXCLUDE)));

        context.setRawRequest(request);
    }
}