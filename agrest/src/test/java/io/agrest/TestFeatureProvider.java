package io.agrest;

import org.apache.cayenne.di.Injector;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;

import static org.junit.Assert.assertNotNull;

public class TestFeatureProvider implements AgFeatureProvider {

    @Override
    public Feature feature(Injector injector) {
        assertNotNull(injector);
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
