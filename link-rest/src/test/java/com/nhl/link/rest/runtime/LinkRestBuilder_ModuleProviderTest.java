package com.nhl.link.rest.runtime;

import org.apache.cayenne.di.Binder;
import org.apache.cayenne.di.Module;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Consumer;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LinkRestBuilder_ModuleProviderTest {

    @Test
    public void testLrModule_Provider() {
        assertRuntime(
                new LinkRestBuilder().module(new TestLrModuleProvider()),
                this::assertTestServiceMapped);
    }

    @Test
    public void testModule() {
        assertRuntime(
                new LinkRestBuilder().module(new TestModule()),
                this::assertTestServiceMapped);
    }

    private void assertTestServiceMapped(LinkRestRuntime runtime) {
        TestService s = runtime.service(TestService.class);
        assertNotNull(s);
        assertTrue(s instanceof TestServiceImpl);
    }

    private void assertRuntime(LinkRestBuilder builder, Consumer<LinkRestRuntime> test) {
        LinkRestRuntime r = builder.build();
        try {
            test.accept(r);
        } finally {
            r.shutdown();
        }
    }

    static class TestLrModuleProvider implements LrModuleProvider {

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
    }

    interface TestService {
    }

    public static class TestServiceImpl implements TestService {
    }

    public static class TestModule implements Module {
        @Override
        public void configure(Binder binder) {
            binder.bind(TestService.class).to(TestServiceImpl.class);
        }
    }
}
