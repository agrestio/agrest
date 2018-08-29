package com.nhl.link.rest.runtime.processor.select;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.protocol.Dir;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.protocol.Sort;
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
        LrRequest request = context.getRequest();
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        LrRequest.Builder requestBuilder = LrRequest.builder()
                .cayenneExp(getCayenneExp(request, protocolParameters))
                .sort(getSort(request, protocolParameters))
                .sortDirection(getSortDirection(request, protocolParameters))
                .mapBy(getMapBy(request, protocolParameters))
                .includes(getIncludes(request, protocolParameters))
                .excludes(getExcludes(request, protocolParameters))
                .start(getStart(request, protocolParameters))
                .limit(getLimit(request, protocolParameters));

        context.setRawRequest(requestBuilder.build());
    }

    private CayenneExp getCayenneExp(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && request.getCayenneExp() != null ?
                request.getCayenneExp() :
                expParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_CAYENNE_EXP));
    }
    private Sort getSort(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && request.getSort() != null ?
                request.getSort() :
                sortParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_SORT));
    }

    private Dir getSortDirection(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && request.getSortDirection() != null ?
                request.getSortDirection() :
                sortParser.dirFromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_DIR));
    }

    private MapBy getMapBy(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && request.getMapBy() != null ?
                request.getMapBy() :
                mapByParser.fromString(ParameterExtractor.string(protocolParameters, PROTOCOL_KEY_MAP_BY));
    }

    private List<Include> getIncludes(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getIncludes().isEmpty() ?
                request.getIncludes() :
                includeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_INCLUDE));
    }

    private List<Exclude> getExcludes(LrRequest request, Map<String, List<String>> protocolParameters) {
        return request != null && !request.getExcludes().isEmpty() ?
                request.getExcludes() :
                excludeParser.fromStrings(ParameterExtractor.strings(protocolParameters, PROTOCOL_KEY_EXCLUDE));
    }

    private Start getStart(LrRequest request, Map<String, List<String>> protocolParameters) {
        int start = ParameterExtractor.integer(protocolParameters, PROTOCOL_KEY_START);
        if (request != null && request.getStart() != null) {
            return request.getStart();
        } else if (start >= 0) {
            return new Start(start);
        }

        return null;
    }

    private Limit getLimit(LrRequest request, Map<String, List<String>> protocolParameters) {
        int limit = ParameterExtractor.integer(protocolParameters, PROTOCOL_KEY_LIMIT);
        if (request != null && request.getLimit() != null) {
            return request.getLimit();
        } else if (limit >= 0) {
            return new Limit(limit);
        }

        return null;
    }
}