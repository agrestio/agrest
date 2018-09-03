package io.agrest;

import org.apache.cayenne.di.Module;

/**
 * @since 2.11
 * @deprecated since 2.11 in favor of using new date encoding strategy (default in the core module)
 */
@Deprecated
public class LegacyDateEncodersModuleProvider implements LrModuleProvider {

    @Override
    public Module module() {
        return new LegacyDateEncodersModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return LegacyDateEncodersModule.class;
    }
}
