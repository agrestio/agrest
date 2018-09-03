package io.agrest.sencha;

import io.agrest.AgModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 2.10
 */
public class SenchaModuleProvider implements AgModuleProvider {

    @Override
    public Module module() {
        return new SenchaModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return SenchaModule.class;
    }
}
