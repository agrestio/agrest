package io.agrest.runtime;

import io.agrest.AgModuleProvider;
import io.agrest.TestModuleProvider;
import io.agrest.encoder.PropertyMetadataEncoder;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class LinkRestBuilder_ModuleProviderTest {

    @Test
    public void testLrModule_Provider() {
        inRuntime(
                new LinkRestBuilder().module(new LocalTestModuleProvider()),
                this::assertLocalTestModuleActive);
    }

    @Test
    public void testModule() {
        inRuntime(
                new LinkRestBuilder().module(new LocalTestModule()),
                this::assertLocalTestModuleActive);
    }

    @Test
    public void testAutoLoading() {
        inRuntime(
                new LinkRestBuilder(),
                this::assertTestModuleActive);
    }

    @Test
    public void testSuppressAutoLoading() {
        inRuntime(
                new LinkRestBuilder().doNotAutoLoadModules(),
                this::assertTestModuleNotActive);
    }

    private void assertLocalTestModuleActive(LinkRestRuntime runtime) {
        Map<String, PropertyMetadataEncoder> encoders =
                runtime.service(Key.getMapOf(String.class, PropertyMetadataEncoder.class));
        assertTrue(encoders.containsKey("local.test"));
    }

    private void assertTestModuleActive(LinkRestRuntime runtime) {
        Map<String, PropertyMetadataEncoder> encoders =
                runtime.service(Key.getMapOf(String.class, PropertyMetadataEncoder.class));
        assertTrue("Auto-loading was off", encoders.containsKey(TestModuleProvider.METADATA_ENCODER_KEY));
    }

    private void assertTestModuleNotActive(LinkRestRuntime runtime) {
        Map<String, PropertyMetadataEncoder> encoders =
                runtime.service(Key.getMapOf(String.class, PropertyMetadataEncoder.class));
        assertFalse("Auto-loading was on", encoders.containsKey(TestModuleProvider.METADATA_ENCODER_KEY));
    }

    private void inRuntime(LinkRestBuilder builder, Consumer<LinkRestRuntime> test) {
        LinkRestRuntime r = builder.build();
        try {
            test.accept(r);
        } finally {
            r.shutdown();
        }
    }

    static class LocalTestModuleProvider implements AgModuleProvider {

        @Override
        public Module module() {
            return new LocalTestModule();
        }

        @Override
        public Class<? extends Module> moduleType() {
            return LocalTestModule.class;
        }

        @Override
        public Collection<Class<? extends Module>> overrides() {
            return Collections.emptyList();
        }
    }

    public static class LocalTestModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bindMap(PropertyMetadataEncoder.class)
                    .put("local.test", mock(PropertyMetadataEncoder.class));
        }
    }
}
