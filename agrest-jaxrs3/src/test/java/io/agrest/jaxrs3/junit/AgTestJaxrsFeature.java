package io.agrest.jaxrs3.junit;

import io.agrest.jaxrs3.AgJaxrsFeature;
import io.agrest.runtime.AgRuntime;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;

import javax.inject.Inject;

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
        AgJaxrsFeature feature = AgJaxrsFeature.build(runtime);
        feature.configure(context);
        return true;
    }
}
