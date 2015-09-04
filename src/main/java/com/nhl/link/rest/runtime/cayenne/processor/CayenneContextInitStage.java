package com.nhl.link.rest.runtime.cayenne.processor;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

/**
 * @since 1.16
 */
public class CayenneContextInitStage<C extends ProcessingContext<T>, T> extends ProcessingStage<C, T> {

	private static final String UPDATE_OBJECT_CONTEXT_ATTRIBITE = "updateContext";

	/**
	 * Returns Cayenne ObjectContext previously stored in the ProcessingContext
	 * by this stage.
	 */
	public static ObjectContext cayenneContext(ProcessingContext<?> context) {
		return (ObjectContext) context.getAttribute(CayenneContextInitStage.UPDATE_OBJECT_CONTEXT_ATTRIBITE);
	}

	private ICayennePersister persister;

	// do we need a listener annotation for this stage? It is kind of trivial,
	// and any possible uses of a listener can probably be addressed with
	// @UpdateChainInitialized

	public CayenneContextInitStage(Processor<C, ? super T> next, ICayennePersister persister) {
		super(next);
		this.persister = persister;
	}

	@Override
	protected void doExecute(C context) {
		context.setAttribute(UPDATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
	}
}
