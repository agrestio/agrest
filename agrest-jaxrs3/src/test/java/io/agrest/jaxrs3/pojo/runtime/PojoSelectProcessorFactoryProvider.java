package io.agrest.jaxrs3.pojo.runtime;

import io.agrest.SelectStage;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.AgExceptionMappers;
import io.agrest.runtime.processor.select.stage.SelectApplyServerParamsStage;
import io.agrest.runtime.processor.select.stage.SelectCreateResourceEntityStage;
import io.agrest.runtime.processor.select.stage.SelectEncoderInstallStage;
import io.agrest.runtime.processor.select.SelectContext;
import io.agrest.runtime.processor.select.SelectProcessorFactory;
import io.agrest.runtime.processor.select.stage.SelectStartStage;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

public class PojoSelectProcessorFactoryProvider implements Provider<SelectProcessorFactory> {

    private final AgExceptionMappers exceptionMappers;
    private final EnumMap<SelectStage, Processor<SelectContext<?>>> stages;

    public PojoSelectProcessorFactoryProvider(
            @Inject SelectStartStage startStage,
            @Inject SelectCreateResourceEntityStage createResourceEntityStage,
            @Inject SelectApplyServerParamsStage applyServerParamsStage,
            @Inject PojoFetchStage pojoFetchStage,
            @Inject SelectEncoderInstallStage encoderStage,

            @Inject AgExceptionMappers exceptionMappers) {

        this.exceptionMappers = exceptionMappers;

        stages = new EnumMap<>(SelectStage.class);
        stages.put(SelectStage.START, startStage);
        stages.put(SelectStage.CREATE_ENTITY, createResourceEntityStage);
        stages.put(SelectStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        stages.put(SelectStage.ASSEMBLE_QUERY, c -> ProcessorOutcome.CONTINUE);
        stages.put(SelectStage.FETCH_DATA, pojoFetchStage);
        stages.put(SelectStage.ENCODE, encoderStage);
    }

    @Override
    public SelectProcessorFactory get() throws DIRuntimeException {
        return new SelectProcessorFactory(stages, exceptionMappers);
    }


}
