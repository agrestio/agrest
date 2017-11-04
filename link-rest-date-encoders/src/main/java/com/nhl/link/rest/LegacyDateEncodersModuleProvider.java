package com.nhl.link.rest;

import org.apache.cayenne.di.Module;

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
