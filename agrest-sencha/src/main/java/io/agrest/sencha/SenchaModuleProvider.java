package io.agrest.sencha;

import io.agrest.LrModuleProvider;
import org.apache.cayenne.di.Module;

/**
 * @since 2.10
 */
public class SenchaModuleProvider implements LrModuleProvider {

    @Override
    public Module module() {
        return new SenchaModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return SenchaModule.class;
    }
}
