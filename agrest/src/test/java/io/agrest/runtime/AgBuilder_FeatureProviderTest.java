package io.agrest.runtime;

import io.agrest.AgFeatureProvider;
import io.agrest.TestFeatureProvider;
import org.apache.cayenne.di.Injector;
import org.junit.Test;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgBuilder_FeatureProviderTest {

    @Test
    public void testFeature() {
        inRuntime(
                new AgBuilder().feature(new LocalTestFeature()),
                this::assertLocalTestFeatureActive);
    }

    @Test
    public void testFeatureProvider() {
        inRuntime(
                new AgBuilder().feature(new LocalTestFeatureProvider()),
                this::assertLocalTestFeatureActive);
    }

    @Test
    public void testAutoLoadFeaturesDefault() {
        inRuntime(
                new AgBuilder(),
                this::assertTestFeatureActive);
    }

    @Test
    public void testDoNotAutoLoadFeatures() {
        inRuntime(
                new AgBuilder().doNotAutoLoadFeatures(),
                this::assertTestFeatureNotActive);
    }

    private void assertTestFeatureActive(AgRuntime runtime) {
        Set<Object> registered = extractRegisteredInJaxRS(runtime);
        assertTrue(registered.contains(TestFeatureProvider.RegisteredByFeature.class));
    }

    private void assertTestFeatureNotActive(AgRuntime runtime) {
        Set<Object> registered = extractRegisteredInJaxRS(runtime);
        assertFalse("Auto-loading was on", registered.contains(TestFeatureProvider.RegisteredByFeature.class));
    }

    private void assertLocalTestFeatureActive(AgRuntime runtime) {
        Set<Object> registered = extractRegisteredInJaxRS(runtime);
        assertTrue("Auto-loading was off", registered.contains(LocalRegisteredByFeature.class));
    }

    private Set<Object> extractRegisteredInJaxRS(AgRuntime runtime) {
        Set<Object> registered = new HashSet<>();
        FeatureContext fc = mock(FeatureContext.class);
        when(fc.register(any(Class.class))).then(i -> {
            registered.add(i.getArguments()[0]);
            return fc;
        });

        runtime.configure(fc);
        return registered;
    }

    private void inRuntime(AgBuilder builder, Consumer<AgRuntime> test) {
        AgRuntime r = builder.build();
        try {
            test.accept(r);
        } finally {
            r.shutdown();
        }
    }

    static class LocalTestFeatureProvider implements AgFeatureProvider {

        @Override
        public Feature feature(Injector injector) {
            assertNotNull(injector);
            return new LocalTestFeature();
        }
    }

    static class LocalTestFeature implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            context.register(LocalRegisteredByFeature.class);
            return false;
        }
    }

    static class LocalRegisteredByFeature {

    }
}
