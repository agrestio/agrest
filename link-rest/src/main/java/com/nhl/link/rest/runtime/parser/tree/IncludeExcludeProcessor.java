package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.CountProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.FunctionProcessor;
import org.apache.cayenne.di.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since 1.5
 */
public class IncludeExcludeProcessor extends BaseRequestProcessor implements ITreeProcessor {

    private static final String INCLUDE = "include";
    private static final String EXCLUDE = "exclude";

    private static final String COUNT_FN = "count";

    private IncludeWorker includeWorker;
    private ExcludeWorker excludeWorker;

    public IncludeExcludeProcessor(@Inject IJacksonService jacksonService, @Inject ISortProcessor sortProcessor,
                                   @Inject ICayenneExpProcessor expProcessor) {
        Map<String, FunctionProcessor> functionProcessors = new HashMap<>();
        functionProcessors.put(COUNT_FN, new CountProcessor());

        this.includeWorker = new IncludeWorker(jacksonService, sortProcessor, expProcessor, functionProcessors);
        this.excludeWorker = new ExcludeWorker(jacksonService);
    }

    @Override
    public void process(ResourceEntity<?> entity, Map<String, List<String>> protocolParameters) {

        // process even if no parameters exist ... this will result in
        // default includes

        includeWorker.process(entity, strings(protocolParameters, INCLUDE));
        excludeWorker.process(entity, strings(protocolParameters, EXCLUDE));
    }
}
