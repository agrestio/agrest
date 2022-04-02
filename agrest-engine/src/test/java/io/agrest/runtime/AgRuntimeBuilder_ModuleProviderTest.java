package io.agrest.runtime;

import com.fasterxml.jackson.databind.JsonNode;
import io.agrest.AgModuleProvider;
import io.agrest.TestModuleProvider;
import io.agrest.converter.jsonvalue.JsonValueConverter;
import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Key;
import org.apache.cayenne.di.Module;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AgRuntimeBuilder_ModuleProviderTest {

    static final String CONVERTER_KEY = X.class.getName();

    @Test
    public void testAgModule_Provider() {
        inRuntime(
                new AgRuntimeBuilder().module(new LocalTestModuleProvider()),
                this::assertLocalTestModuleActive);
    }

    @Test
    public void testModule() {
        inRuntime(
                new AgRuntimeBuilder().module(new LocalTestModule()),
                this::assertLocalTestModuleActive);
    }

    @Test
    public void testAutoLoading() {
        inRuntime(
                new AgRuntimeBuilder(),
                this::assertTestModuleActive);
    }

    @Test
    public void testSuppressAutoLoading() {
        inRuntime(
                new AgRuntimeBuilder().doNotAutoLoadModules(),
                this::assertTestModuleNotActive);
    }

    private void assertLocalTestModuleActive(AgRuntime runtime) {
        Map<String, JsonValueConverter> converters =
                runtime.service(Key.getMapOf(String.class, JsonValueConverter.class));
        assertTrue(converters.containsKey(CONVERTER_KEY));
    }

    private void assertTestModuleActive(AgRuntime runtime) {
        Map<String, JsonValueConverter> converters =
                runtime.service(Key.getMapOf(String.class, JsonValueConverter.class));
        assertTrue(converters.containsKey(TestModuleProvider.CONVERTER_KEY), "Auto-loading was off");
    }

    private void assertTestModuleNotActive(AgRuntime runtime) {
        Map<String, JsonValueConverter> converters =
                runtime.service(Key.getMapOf(String.class, JsonValueConverter.class));
        assertFalse(converters.containsKey(TestModuleProvider.CONVERTER_KEY), "Auto-loading was on");
    }

    private void inRuntime(AgRuntimeBuilder builder, Consumer<AgRuntime> test) {
        AgRuntime r = builder.build();
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
    }

    public static class LocalTestModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bindMap(JsonValueConverter.class).put(CONVERTER_KEY, new XConverter());
        }
    }

    public static class X {
    }

    public static class XConverter implements JsonValueConverter<X> {
        @Override
        public X value(JsonNode node) {
            return null;
        }
    }
}
