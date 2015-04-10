package com.nhl.link.rest.runtime.processor;

import java.util.Map;

import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.16
 */
public interface IProcessorFactory {

	/**
	 * Returns a map of processors by context type and operation name.
	 */
	Map<Class<?>, Map<String, Processor<?, ?>>> processors();

}
