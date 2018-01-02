package com.nhl.link.rest.runtime.parser.tree;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.runtime.jackson.IJacksonService;
import com.nhl.link.rest.runtime.parser.BaseRequestProcessor;
import com.nhl.link.rest.runtime.parser.filter.ICayenneExpProcessor;
import com.nhl.link.rest.runtime.parser.sort.ISortProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.AverageProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.CountProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.FunctionProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.MaximumProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.MinimumProcessor;
import com.nhl.link.rest.runtime.parser.tree.function.SumProcessor;
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
    private static final String AVERAGE_FN = "avg";
    private static final String SUM_FN = "sum";
    private static final String MINIMUM_FN = "min";
    private static final String MAXIMUM_FN = "max";

    private IncludeWorker includeWorker;
    private ExcludeWorker excludeWorker;

    public IncludeExcludeProcessor(@Inject IJacksonService jacksonService, @Inject ISortProcessor sortProcessor,
                                   @Inject ICayenneExpProcessor expProcessor) {
        Map<String, FunctionProcessor> functionProcessors = new HashMap<>();
        functionProcessors.put(COUNT_FN, new CountProcessor());
        functionProcessors.put(AVERAGE_FN, new AverageProcessor());
        functionProcessors.put(SUM_FN, new SumProcessor());
        functionProcessors.put(MINIMUM_FN, new MinimumProcessor());
        functionProcessors.put(MAXIMUM_FN, new MaximumProcessor());

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
