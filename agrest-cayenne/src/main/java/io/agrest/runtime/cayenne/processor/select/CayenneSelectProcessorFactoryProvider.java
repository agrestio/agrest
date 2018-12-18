package io.agrest.runtime.cayenne.processor.select;

import io.agrest.SelectStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;
import org.apache.cayenne.query.PrefetchTreeNode;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneSelectProcessorFactoryProvider implements Provider<SelectProcessorFactory> {
    
    private EnumMap<SelectStage, Processor<SelectContext<?, ?>>> stages;

    public CayenneSelectProcessorFactoryProvider(
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject ApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneFetchDataStage fetchDataStage) {

        stages = new EnumMap<>(SelectStage.class);

        stages.put(SelectStage.START,
                    (SelectContext<?, ?> c) -> {
                            c.setPrefetchSemantics(PrefetchTreeNode.DISJOINT_PREFETCH_SEMANTICS);
                            return ProcessorOutcome.CONTINUE; });

        stages.put(SelectStage.PARSE_REQUEST, parseRequestStage);
        stages.put(SelectStage.CREATE_ENTITY, createResourceEntityStage);
        stages.put(SelectStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        stages.put(SelectStage.FETCH_DATA, fetchDataStage);
    }

    @Override
    public SelectProcessorFactory get() throws DIRuntimeException {
        return new SelectProcessorFactory(stages);
    }
}
