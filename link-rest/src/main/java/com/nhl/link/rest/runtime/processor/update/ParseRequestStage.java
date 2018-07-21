package com.nhl.link.rest.runtime.processor.update;

import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.processor.ProcessorOutcome;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.tree.IExcludeParser;
import com.nhl.link.rest.runtime.parser.tree.IIncludeParser;
import com.nhl.link.rest.protocol.Exclude;
import com.nhl.link.rest.protocol.Include;
import com.nhl.link.rest.protocol.Query;
import org.apache.cayenne.di.Inject;

import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class ParseRequestStage implements Processor<UpdateContext<?>> {

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

        Query query = new Query(null, null, null, null, null, null,
                includeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Include.INCLUDE)),
                excludeParser.fromStrings(BaseRequestProcessor.strings(protocolParameters, Exclude.EXCLUDE)));

        context.setRawQuery(query);
    }
}
