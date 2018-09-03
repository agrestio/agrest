package io.agrest.runtime.cayenne.processor.update;

import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneCreateStage extends CayenneUpdateDataStoreStage {

    public CayenneCreateStage(@Inject IMetadataService metadataService) {
        super(metadataService);
    }

    @Override
    protected <T extends DataObject> void sync(UpdateContext<T> context) {
        create(context);
    }
}
