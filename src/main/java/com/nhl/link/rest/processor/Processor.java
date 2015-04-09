package com.nhl.link.rest.processor;

/**
 * A request processor. The most common processors in LinkRest are based on a
 * chain-of-responsibility pattern and inherit from {@link ProcessingStage}.
 * 
 * @since 1.16
 */
public interface Processor<C extends ProcessingContext<?>> {

	void execute(C context);
}
