package io.agrest.sencha.runtime.processor.select;

import io.agrest.AgRequest;
import io.agrest.AgRequestBuilder;
import io.agrest.protocol.Sort;
import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.runtime.protocol.ISenchaFilterParser;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class SenchaParseRequestStage extends ParseRequestStage {

    static final String FILTER = "filter";
    static final String GROUP = "group";
    static final String GROUP_DIR = "groupDir";

    private ISenchaFilterParser filterParser;

    public SenchaParseRequestStage(
            @Inject IAgRequestBuilderFactory requestBuilderFactory,
            @Inject ISenchaFilterParser filterParser) {

        super(requestBuilderFactory);
        this.filterParser = filterParser;
    }

    @Override
    protected <T> void doExecute(SelectContext<T> context) {

        super.doExecute(context);

        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        // TODO: make symmetrical with AgRequestBuilder (namely hide the parser inside the builder)
        //  Or even better - convert "filter" to CayenneExp and avoid this SenchaRequest buisness all together
        SenchaRequest.Builder builder = SenchaRequest.builder()
                .filters(filterParser.fromString(ParameterExtractor.string(protocolParameters, FILTER)));

        SenchaRequest.set(context, builder.build());
    }

    @Override
    protected AgRequestBuilder requestFromParams(Map<String, List<String>> parameters) {

        AgRequestBuilder builder = super.requestFromParams(parameters);

        Sort altSorter = altSorter(parameters);
        if (altSorter != null) {
            builder.sort(altSorter);
        }

        return builder;
    }

    @Override
    protected AgRequestBuilder requestFromRequestAndParams(AgRequest request, Map<String, List<String>> parameters) {
        AgRequestBuilder builder = super.requestFromRequestAndParams(request, parameters);

        // if we are allowed to override
        if (request.getSort() == null) {
            Sort altSorter = altSorter(parameters);
            if (altSorter != null) {
                builder.sort(altSorter);
            }
        }

        return builder;
    }

    private Sort altSorter(Map<String, List<String>> parameters) {

        // Sencha introduces an extra "group" sorter that needs to be merged with regular sorter
        Sort senchaSorter = requestBuilderFactory.builder()
                .sort(ParameterExtractor.string(parameters, GROUP), ParameterExtractor.string(parameters, GROUP_DIR))
                .build()
                .getSort();

        if (senchaSorter == null) {
            return null;
        }

        Sort sorter = requestBuilderFactory.builder()
                .sort(ParameterExtractor.string(parameters, PROTOCOL_SORT), ParameterExtractor.string(parameters, PROTOCOL_DIR))
                .build().getSort();

        // merge group and sort; group goes first
        return sorter != null ? new Sort(asList(senchaSorter, sorter)) : senchaSorter;
    }
}
