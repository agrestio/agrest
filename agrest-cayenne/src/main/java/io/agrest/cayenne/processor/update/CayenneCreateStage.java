package io.agrest.cayenne.processor.update;

import io.agrest.cayenne.persister.ICayennePersister;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.update.UpdateContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.di.Inject;

/**
 * @since 2.7
 */
public class CayenneCreateStage extends CayenneUpdateDataStoreStage {

    public CayenneCreateStage(
            @Inject IMetadataService metadataService,
            @Inject ICayennePersister persister) {
        super(metadataService, persister.entityResolver());
    }

    @Override
    protected <T extends DataObject> void sync(UpdateContext<T> context) {
        create(context);
    }
}
