package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

    protected static final String PROTOCOL_KEY_EXCLUDE = "exclude";
    protected static final String PROTOCOL_KEY_INCLUDE = "include";

    private IIncludeParser includeParser;
    private IExcludeParser excludeParser;

    public ParseRequestStage(
            @Inject IIncludeParser includeParser,
            @Inject IExcludeParser excludeParser) {

        this.includeParser = includeParser;
        this.excludeParser = excludeParser;
    }

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        // TODO: should we skip this for SimpleResponse-returning updates?
        Map<String, List<String>> protocolParameters = context.getProtocolParameters();

        LrRequest request = LrRequest.builder()
                .includes(includeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, PROTOCOL_KEY_INCLUDE)))
                .excludes(excludeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, PROTOCOL_KEY_EXCLUDE)))
                .build();

        context.setRawRequest(request);
    }
}
