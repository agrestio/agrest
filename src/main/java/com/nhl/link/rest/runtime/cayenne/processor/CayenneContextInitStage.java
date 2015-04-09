package com.nhl.link.rest.runtime.cayenne.processor;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.cayenne.ICayennePersister;

/**
 * @since 1.16
 */
public class CayenneContextInitStage<C extends ProcessingContext<?>> extends ProcessingStage<C> {

	private static final String UPDATE_OBJECT_CONTEXT_ATTRIBITE = "updateContext";

	/**
	 * Returns Cayenne ObjectContext previously stored in the ProcessingContext
	 * by this stage.
	 */
	public static ObjectContext cayenneContext(ProcessingContext<?> context) {
		return (ObjectContext) context.getAttribute(CayenneContextInitStage.UPDATE_OBJECT_CONTEXT_ATTRIBITE);
	}

	private ICayennePersister persister;

	public CayenneContextInitStage(Processor<C> next, ICayennePersister persister) {
		super(next);
		this.persister = persister;
	}

	@Override
	protected void doExecute(C context) {
		context.setAttribute(UPDATE_OBJECT_CONTEXT_ATTRIBITE, persister.newContext());
	}
}
