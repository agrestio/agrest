package com.nhl.link.rest.runtime.cayenne.processor;

import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.DataObject;

import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

/**
 * @since 1.16
 */
public class CayenneCreateStage<T extends DataObject> extends BaseCayenneUpdateStage<T> {

	public CayenneCreateStage(ProcessingStage<UpdateContext<T>, ? super T> next, IMetadataService metadataService) {
		super(next, metadataService);
	}

	@Override
	protected void sync(UpdateContext<T> context) {
		create(context);
	}

}
