package io.agrest.jaxrs3;

import io.agrest.jaxrs3.AgFeatureProvider;
import io.agrest.runtime.AgRuntime;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestFeatureProvider implements AgFeatureProvider {

    @Override
    public Feature feature(AgRuntime runtime) {
        assertNotNull(runtime);
        return new TestFeature();
    }

    public static class TestFeature implements Feature {

        @Override
        public boolean configure(FeatureContext context) {
            context.register(RegisteredByFeature.class);
            return false;
        }
    }

    public static class RegisteredByFeature {

    }
}
