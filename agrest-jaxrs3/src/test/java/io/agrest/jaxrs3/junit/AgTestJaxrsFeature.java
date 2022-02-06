package io.agrest.jaxrs3.junit;

import io.agrest.jaxrs3.AgJaxrsFeature;
import io.agrest.runtime.AgRuntime;

import javax.inject.Inject;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

/**
 * Integrates {@link AgJaxrsFeature} into Bootique test stack.
 */
public class AgTestJaxrsFeature implements Feature {

    private final AgRuntime runtime;

    @Inject
    public AgTestJaxrsFeature(AgRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public boolean configure(FeatureContext context) {
        AgJaxrsFeature feature = AgJaxrsFeature.builder().runtime(runtime).build();
        feature.configure(context);
        return true;
    }
}
