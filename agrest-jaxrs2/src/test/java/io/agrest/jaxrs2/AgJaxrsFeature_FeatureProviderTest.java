package io.agrest.jaxrs2;

import io.agrest.runtime.AgRuntime;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AgJaxrsFeature_FeatureProviderTest {

    @Test
    public void testFeature() {
        inRuntime(
                AgJaxrsFeature.builder().feature(new LocalTestFeature()),
                this::assertLocalTestFeatureActive);
    }

    @Test
    public void testFeatureProvider() {
        inRuntime(
                AgJaxrsFeature.builder().feature(new LocalTestFeatureProvider()),
                this::assertLocalTestFeatureActive);
    }

    @Test
    public void testAutoLoadFeaturesDefault() {
        inRuntime(
                AgJaxrsFeature.builder(),
                this::assertTestFeatureActive);
    }

    @Test
    public void testDoNotAutoLoadFeatures() {
        inRuntime(
                AgJaxrsFeature.builder().doNotAutoLoadFeatures(),
                this::assertTestFeatureNotActive);
    }

    private void assertTestFeatureActive(AgJaxrsFeature feature) {
        Set<Object> registered = extractRegisteredInJaxRS(feature);
        assertTrue(registered.contains(TestFeatureProvider.RegisteredByFeature.class));
    }

    private void assertTestFeatureNotActive(AgJaxrsFeature feature) {
        Set<Object> registered = extractRegisteredInJaxRS(feature);
        assertFalse(registered.contains(TestFeatureProvider.RegisteredByFeature.class), "Auto-loading was on");
    }

    private void assertLocalTestFeatureActive(AgJaxrsFeature feature) {
        Set<Object> registered = extractRegisteredInJaxRS(feature);
        assertTrue(registered.contains(LocalRegisteredByFeature.class), "Auto-loading was off");
    }

    private Set<Object> extractRegisteredInJaxRS(AgJaxrsFeature feature) {
        Set<Object> registered = new HashSet<>();
        FeatureContext fc = mock(FeatureContext.class);
        when(fc.register(any(Class.class))).then(i -> {
            registered.add(i.getArguments()[0]);
            return fc;
        });

        feature.configure(fc);
        return registered;
    }

    private void inRuntime(AgJaxrsFeatureBuilder builder, Consumer<AgJaxrsFeature> test) {
        AgJaxrsFeature feature = builder.build();
        try {
            test.accept(feature);
        } finally {
            feature.getRuntime().shutdown();
        }
    }

    static class LocalTestFeatureProvider implements AgFeatureProvider {

        @Override
        public Feature feature(AgRuntime runtime) {
            assertNotNull(runtime);
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
