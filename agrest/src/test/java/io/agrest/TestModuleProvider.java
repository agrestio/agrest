package io.agrest;

import io.agrest.encoder.PropertyMetadataEncoder;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.mock;

public class TestModuleProvider implements AgModuleProvider {

    public static final String METADATA_ENCODER_KEY = "TestModuleProvider.test";

    @Override
    public Module module() {
        return new TestModule();
    }

    @Override
    public Class<? extends Module> moduleType() {
        return TestModule.class;
    }

    @Override
    public Collection<Class<? extends Module>> overrides() {
        return Collections.emptyList();
    }

    public static class TestModule implements Module {

        @Override
        public void configure(Binder binder) {
            binder.bindMap(PropertyMetadataEncoder.class)
                    .put(METADATA_ENCODER_KEY, mock(PropertyMetadataEncoder.class));
        }
    }
}
