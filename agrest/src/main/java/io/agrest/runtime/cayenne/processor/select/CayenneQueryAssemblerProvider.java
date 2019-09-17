package io.agrest.runtime.cayenne.processor.select;

import io.agrest.runtime.cayenne.ICayennePersister;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.di.Provider;

/**
 * @since 3.4
 */
public class CayenneQueryAssemblerProvider implements Provider<CayenneQueryAssembler> {

    private ICayennePersister persister;

    public CayenneQueryAssemblerProvider(@Inject ICayennePersister persister) {
        this.persister = persister;
    }

    @Override
    public CayenneQueryAssembler get() {
        return new CayenneQueryAssembler(persister.entityResolver());
    }
}
