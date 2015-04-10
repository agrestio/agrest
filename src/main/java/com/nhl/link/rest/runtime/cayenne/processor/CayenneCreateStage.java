package com.nhl.link.rest.runtime.cayenne.processor;

import org.apache.cayenne.DataObject;

import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneCreateStage<T extends DataObject> extends BaseCayenneUpdateStage<T> {

	public CayenneCreateStage(Processor<UpdateContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void sync(UpdateContext<T> context) {
		create(context);
	}

}
