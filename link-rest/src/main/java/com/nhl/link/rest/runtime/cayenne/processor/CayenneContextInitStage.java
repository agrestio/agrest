package com.nhl.link.rest.runtime.cayenne.processor;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

/**
 * @since 1.16
 * @deprecated since 2.7 in favor of {@link com.nhl.link.rest.runtime.cayenne.processor.update.CayenneStartStage}.
 */
public class CayenneContextInitStage<C extends ProcessingContext<T>, T> extends BaseLinearProcessingStage<C, T> {

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

	public CayenneContextInitStage(ProcessingStage<C, ? super T> next, ICayennePersister persister) {
		super(next);
		this.persister = persister;
	}

	@Override
	protected void doExecute(C context) {
		context.setAttribute(UPDATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
	}
}
