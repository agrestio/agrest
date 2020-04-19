package io.agrest.it.fixture.pojo;

import io.agrest.SelectStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.processor.select.ApplyServerParamsStage;
import io.agrest.runtime.processor.select.CreateResourceEntityStage;
import io.agrest.runtime.processor.select.ParseRequestStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.select.StartStage;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

public class PojoSelectProcessorFactoryProvider implements Provider<SelectProcessorFactory> {

    private EnumMap<SelectStage, Processor<SelectContext<?>>> stages;

    public PojoSelectProcessorFactoryProvider(
            @Inject StartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject ApplyServerParamsStage applyServerParamsStage,
            @Inject PojoFetchStage pojoFetchStage) {

        stages = new EnumMap<>(SelectStage.class);
        stages.put(SelectStage.START, startStage);
        stages.put(SelectStage.PARSE_REQUEST, parseRequestStage);
        stages.put(SelectStage.CREATE_ENTITY, createResourceEntityStage);
        stages.put(SelectStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        stages.put(SelectStage.ASSEMBLE_QUERY, c -> ProcessorOutcome.CONTINUE);
        stages.put(SelectStage.FETCH_DATA, pojoFetchStage);
    }

    @Override
    public SelectProcessorFactory get() throws DIRuntimeException {
        return new SelectProcessorFactory(stages);
    }


}
