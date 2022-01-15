package io.agrest.runtime.processor.select;

import io.agrest.SelectStage;
import io.agrest.processor.Processor;
import io.agrest.runtime.AgExceptionMappers;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class SelectProcessorFactoryProvider implements Provider<SelectProcessorFactory> {

    private final EnumMap<SelectStage, Processor<SelectContext<?>>> stages;
    private final AgExceptionMappers exceptionMappers;

    public SelectProcessorFactoryProvider(
            @Inject StartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject CreateResourceEntityStage createResourceEntityStage,
            @Inject ApplyServerParamsStage applyServerParamsStage,
            @Inject AssembleQueryStage assembleQueryStage,
            @Inject FetchDataStage fetchDataStage,
            @Inject FilterResultStage filterResultStage,
            @Inject EncoderInstallStage encoderInstallStage,
            @Inject AgExceptionMappers exceptionMappers) {

        this.exceptionMappers = exceptionMappers;

        stages = new EnumMap<>(SelectStage.class);
        stages.put(SelectStage.START, startStage);
        stages.put(SelectStage.PARSE_REQUEST, parseRequestStage);
        stages.put(SelectStage.CREATE_ENTITY, createResourceEntityStage);
        stages.put(SelectStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        stages.put(SelectStage.ASSEMBLE_QUERY, assembleQueryStage);
        stages.put(SelectStage.FETCH_DATA, fetchDataStage);
        stages.put(SelectStage.FILTER_RESULT, filterResultStage);
        stages.put(SelectStage.ENCODE, encoderInstallStage);
    }

    @Override
    public SelectProcessorFactory get() throws DIRuntimeException {
        return new SelectProcessorFactory(stages, exceptionMappers);
    }
}
