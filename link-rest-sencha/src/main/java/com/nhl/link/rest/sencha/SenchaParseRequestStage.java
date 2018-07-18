package com.nhl.link.rest.sencha;

import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.query.CayenneExp;
import com.nhl.link.rest.runtime.query.Dir;
import com.nhl.link.rest.runtime.query.Exclude;
import com.nhl.link.rest.runtime.query.Include;
import com.nhl.link.rest.runtime.query.Limit;
import com.nhl.link.rest.runtime.query.MapBy;
import com.nhl.link.rest.runtime.query.Query;
import com.nhl.link.rest.runtime.query.Start;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

public class SenchaParseRequestStage extends ParseRequestStage {

    static final String GROUP = "group";
    static final String GROUP_DIR = "groupDir";

    private ICayenneExpParser expParser;
    private ISortParser sortParser;
    private IMapByParser mapByParser;
    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public SenchaParseRequestStage(
            @Inject ICayenneExpParser expParser,
            @Inject ISortParser sortParser,
            @Inject IMapByParser mapByParser,
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser) {

        super(expParser, sortParser, mapByParser, includeParser, excludeParser);
        this.expParser = expParser;
        this.sortParser = sortParser;
        this.mapByParser = mapByParser;
        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        Query query = new Query(
                expParser.fromString(BaseRequestProcessor.string(protocolParameters, CayenneExp.CAYENNE_EXP)),
                sortParser.fromString(BaseRequestProcessor.string(protocolParameters, GROUP),
                        BaseRequestProcessor.string(protocolParameters, GROUP_DIR)),
                sortParser.dirFromString(BaseRequestProcessor.string(protocolParameters, Dir.DIR)),
                mapByParser.fromString(BaseRequestProcessor.string(protocolParameters, MapBy.MAP_BY)),
                new Start(BaseRequestProcessor.integer(protocolParameters, Start.START)),
                new Limit(BaseRequestProcessor.integer(protocolParameters, Limit.LIMIT)),
                includeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Include.INCLUDE)),
                excludeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Exclude.EXCLUDE)));

        context.setRawQuery(query);
    }
}
