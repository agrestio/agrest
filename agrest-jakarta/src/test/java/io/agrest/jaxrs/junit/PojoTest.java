package io.agrest.jaxrs.junit;

import io.bootique.junit5.BQTest;

/**
 * An abstract superclass of integration tests that starts Bootique test runtime with JAX-RS service and an in-memory
 * "pojo database".
 */
@BQTest
public abstract class PojoTest {

    protected static AgPojoTester.Builder tester(Class<?>... resources) {
        return AgPojoTester
                .builder()
                .resources(resources);
    }
}
