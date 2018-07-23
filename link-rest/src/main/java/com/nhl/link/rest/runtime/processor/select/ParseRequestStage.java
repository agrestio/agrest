package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.Start;
import com.nhl.link.rest.runtime.protocol.ParameterExtractor;
import com.nhl.link.rest.runtime.protocol.ICayenneExpParser;
import com.nhl.link.rest.runtime.protocol.IMapByParser;
import com.nhl.link.rest.runtime.protocol.ISortParser;
import com.nhl.link.rest.runtime.protocol.IExcludeParser;
import com.nhl.link.rest.runtime.protocol.IIncludeParser;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<SelectContext<?>> {

    protected static final String PROTOCOL_KEY_CAYENNE_EXP = "cayenneExp";
    protected static final String PROTOCOL_KEY_DIR = "dir";
    protected static final String PROTOCOL_KEY_EXCLUDE = "exclude";
    protected static final String PROTOCOL_KEY_INCLUDE = "include";
    protected static final String PROTOCOL_KEY_LIMIT = "limit";
    protected static final String PROTOCOL_KEY_MAP_BY = "mapBy";
    protected static final String PROTOCOL_KEY_SORT = "sort";
    protected static final String PROTOCOL_KEY_START = "start";

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

        LrRequest.Builder requestBuilder = LrRequest.builder()
                .cayenneExp(expParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_CAYENNE_EXP)))
                .sort(sortParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_SORT)))
                .sortDirection(sortParser.dirFromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_DIR)))
                .mapBy(mapByParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_MAP_BY)))
                .includes(includeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_INCLUDE)))
                .excludes(excludeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_EXCLUDE)));

        int start = ParameterExtractor.integer(protocolParameters, PROTOCOL_KEY_START);
        if (start >= 0) {
            requestBuilder.start(new Start(start));
        }

        int limit = ParameterExtractor.integer(protocolParameters, PROTOCOL_KEY_LIMIT);
        if (limit >= 0) {
            requestBuilder.limit(new Limit(limit));
        }

        context.setRawRequest(requestBuilder.build());
    }
}