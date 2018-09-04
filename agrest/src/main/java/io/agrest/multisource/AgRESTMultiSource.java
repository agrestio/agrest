package io.agrest.multisource;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.Configuration;

import io.agrest.SelectBuilder;
import io.agrest.runtime.AgRESTRuntime;

/**
 * A builder of multi-source request chains.
 * 
 * @since 2.0
 */
public class AgRESTMultiSource {

	/**
	 * Starts a {@link MultiSelectBuilder} that allows to fetch data from
	 * multiple sources in patallel with minimal or no blocking.
	 * 
	 * @param rootSelectChain
	 *            A root AgREST select chain. Any other results will be
	 *            attached to the response from the root chain.
	 * @param config
	 *            JAX RS configuration that allows the method to locate AgREST
	 *            stack.
	 * @return MultiSelectBuilder instance.
	 */
	public static <T> MultiSelectBuilder<T> select(SelectBuilder<T> rootSelectChain, Configuration config) {

		ExecutorService executor = AgRESTRuntime.service(ExecutorService.class, config);
		return new MultiSelectBuilder<>(rootSelectChain, executor);
	}
}
