package io.agrest.sencha.runtime.processor.select;

import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.protocol.ISortParser;
import io.agrest.runtime.protocol.ParameterExtractor;
import io.agrest.runtime.request.IAgRequestBuilderFactory;
import io.agrest.sencha.SenchaRequest;
import io.agrest.sencha.runtime.protocol.ISenchaFilterParser;
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
            @Inject IAgRequestBuilderFactory requestBuilderFactory,
            @Inject ISortParser sortParser,
            @Inject ISenchaFilterParser filterParser) {

        super(requestBuilderFactory);
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
