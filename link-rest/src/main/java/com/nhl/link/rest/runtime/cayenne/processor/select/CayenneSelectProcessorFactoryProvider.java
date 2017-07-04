package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.select.ApplyServerParamsStage;
import com.nhl.link.rest.runtime.processor.select.ParseRequestStage;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.runtime.processor.select.SelectProcessorFactory;
import com.nhl.link.rest.runtime.processor.select.StartStage;
import org.apache.cayenne.di.DIRuntimeException;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

import java.util.EnumMap;

/**
 * @since 2.7
 */
public class CayenneSelectProcessorFactoryProvider implements Provider<SelectProcessorFactory> {
    
    private EnumMap<SelectStage, Processor<SelectContext<?>>> stages;

    public CayenneSelectProcessorFactoryProvider(
            @Inject StartStage startStage,
            @Inject ParseRequestStage parseRequestStage,
            @Inject ApplyServerParamsStage applyServerParamsStage,
            @Inject CayenneAssembleQueryStage assembleQueryStage,
            @Inject CayenneFetchDataStage fetchDataStage) {

        stages = new EnumMap<>(SelectStage.class);
        stages.put(SelectStage.START, startStage);
        stages.put(SelectStage.PARSE_REQUEST, parseRequestStage);
        stages.put(SelectStage.APPLY_SERVER_PARAMS, applyServerParamsStage);
        stages.put(SelectStage.ASSEMBLE_QUERY, assembleQueryStage);
        stages.put(SelectStage.FETCH_DATA, fetchDataStage);
    }

    @Override
    public SelectProcessorFactory get() throws DIRuntimeException {
        return new SelectProcessorFactory(stages);
    }
}
