package io.agrest.multisource;

import io.agrest.SelectBuilder;
import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Configuration;
import java.util.concurrent.ExecutorService;

/**
 * A builder of multi-source request chains.
 * 
 * @since 2.0
 */
public class AgMultiSource {

	/**
	 * Starts a {@link MultiSelectBuilder} that allows to fetch data from
	 * multiple sources in patallel with minimal or no blocking.
	 * 
	 * @param rootSelectChain
	 *            A root Agrest select chain. Any other results will be
	 *            attached to the response from the root chain.
	 * @param config
	 *            JAX RS configuration that allows the method to locate Agrest
	 *            stack.
	 * @return MultiSelectBuilder instance.
	 */
	public static <T, E> MultiSelectBuilder<T, E> select(SelectBuilder<T, E> rootSelectChain, Configuration config) {

		ExecutorService executor = AgRuntime.service(ExecutorService.class, config);
		return new MultiSelectBuilder<>(rootSelectChain, executor);
	}
}
