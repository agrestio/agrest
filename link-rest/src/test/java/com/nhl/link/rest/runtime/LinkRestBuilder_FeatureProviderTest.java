package com.nhl.link.rest.runtime;

import com.nhl.link.rest.LrFeatureProvider;
import org.apache.cayenne.di.Injector;
import org.junit.Test;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LinkRestBuilder_FeatureProviderTest {

    @Test
    public void testFeature() {
        assertRuntime(
                new LinkRestBuilder().feature(new LocalTestFeature()),
                this::assertLocalTestFeatureActive);
    }

    @Test
    public void testFeatureProvider() {
        assertRuntime(
                new LinkRestBuilder().feature(new LocalTestFeatureProvider()),
                this::assertLocalTestFeatureActive);
    }

    private void assertLocalTestFeatureActive(LinkRestRuntime runtime) {
        Set<Object> registered = new HashSet<>();
        FeatureContext fc = mock(FeatureContext.class);
        when(fc.register(any(Class.class))).then(i -> {
            registered.add(i.getArguments()[0]);
            return fc;
        });

        runtime.configure(fc);
        assertTrue(registered.contains(LocalRegisteredByFeature.class));
    }

    private void assertRuntime(LinkRestBuilder builder, Consumer<LinkRestRuntime> test) {
        LinkRestRuntime r = builder.build();
        try {
            test.accept(r);
        } finally {
            r.shutdown();
        }
    }

    static class LocalTestFeatureProvider implements LrFeatureProvider {

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
