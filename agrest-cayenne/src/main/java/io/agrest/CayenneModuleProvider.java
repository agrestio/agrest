package io.agrest;

import org.apache.cayenne.di.Module;

/**
 *
 *
 */
public class CayenneModuleProvider implements AgModuleProvider {

    @Override
    public Module module() {
        return new CayenneModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return CayenneModule.class;
    }
}