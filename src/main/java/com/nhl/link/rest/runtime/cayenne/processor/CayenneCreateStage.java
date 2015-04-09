package com.nhl.link.rest.runtime.cayenne.processor;

import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneCreateStage extends BaseCayenneUpdateStage {

	public CayenneCreateStage(Processor<UpdateContext<?>> next) {
		super(next);
	}

	@Override
	protected <T> void sync(UpdateContext<T> context) {
		create(context);
	}

}
