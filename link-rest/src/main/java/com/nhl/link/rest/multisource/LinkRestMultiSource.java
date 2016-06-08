package com.nhl.link.rest.multisource;

import java.util.concurrent.ExecutorService;

import javax.ws.rs.core.Configuration;

import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.runtime.LinkRestRuntime;

/**
 * A builder of multi-source request chains.
 * 
 * @since 2.0
 */
public class LinkRestMultiSource {

	/**
	 * Starts a {@link MultiSelectBuilder} that allows to fetch data from
	 * multiple sources in patallel with minimal or no blocking.
	 * 
	 * @param rootSelectChain
	 *            A root LinkRest select chain. Any other results will be
	 *            attached to the response from the root chain.
	 * @param config
	 *            JAX RS configuration that allows the method to locate LinkRest
	 *            stack.
	 * @return MultiSelectBuilder instance.
	 */
	public static <T> MultiSelectBuilder<T> select(SelectBuilder<T> rootSelectChain, Configuration config) {

		ExecutorService executor = LinkRestRuntime.service(ExecutorService.class, config);
		return new MultiSelectBuilder<>(rootSelectChain, executor);
	}
}
