package com.nhl.link.rest.sencha;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.protocol.Start;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpParser;
import com.nhl.link.rest.runtime.parser.mapBy.IMapByParser;
import com.nhl.link.rest.runtime.parser.sort.ISortParser;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
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

        LrRequest.Builder requestBuilder = LrRequest.builder()
                .cayenneExp(expParser.fromString(BaseRequestProcessor.string(protocolParameters, CayenneExp.CAYENNE_EXP)))
                // TODO: GROUP and SORT must be processed together
                .sort(sortParser.fromString(BaseRequestProcessor.string(protocolParameters, GROUP)))
                .sortDirection(sortParser.dirFromString(BaseRequestProcessor.string(protocolParameters, GROUP_DIR)))
                .mapBy(mapByParser.fromString(BaseRequestProcessor.string(protocolParameters, MapBy.MAP_BY)))
                .includes(includeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Include.INCLUDE)))
                .excludes(excludeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Exclude.EXCLUDE)));

        int start = BaseRequestProcessor.integer(protocolParameters, Start.START);
        if (start >= 0) {
            requestBuilder.start(new Start(start));
        }

        int limit = BaseRequestProcessor.integer(protocolParameters, Limit.LIMIT);
        if (limit >= 0) {
            requestBuilder.limit(new Limit(limit));
        }

        context.setRawRequest(requestBuilder.build());
    }
}
