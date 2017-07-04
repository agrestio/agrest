package com.nhl.link.rest.runtime.processor;

import com.nhl.link.rest.processor.ProcessingStage;

import java.util.Map;

/**
 * @since 1.16
 * @deprecated since 2.7 not used for select processors, and the rest will be factored out soon.
 */
public interface IProcessorFactory {

    /**
     * Returns a map of processors by context type and operation name.
     */
    Map<Class<?>, Map<String, ProcessingStage<?, ?>>> processors();

}
