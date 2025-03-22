package io.agrest.jaxrs2;

import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import java.util.Collection;

/**
 * Bootstraps Agrest in the JAX-RS context. Among other things, {@link AgRuntime} is registered as a JAX-RS context
 * property, and becomes accessible via {@link AgJaxrs} static methods.
 *
 * @since 5.0
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public class AgJaxrsFeature implements Feature {

    private static final String JAXRS_AGREST_RUNTIME_PROPERTY = "agrest.runtime";

    private final AgRuntime runtime;
    private final Collection<Class<?>> providers;
    private final Collection<Feature> features;

    /**
     * Builds an AgJaxrsFeature with the provided runtime.
     *
     * @see #builder(AgRuntime)
     */
    public static AgJaxrsFeature build(AgRuntime runtime) {
        return builder(runtime).build();
    }

    /**
     * Starts a builder to configure Agrest for the JAX-RS environment.
     */
    public static AgJaxrsFeatureBuilder builder(AgRuntime runtime) {
        return new AgJaxrsFeatureBuilder(runtime);
    }

    static AgRuntime getRuntime(Configuration configuration) {
        AgRuntime runtime = (AgRuntime) configuration.getProperty(JAXRS_AGREST_RUNTIME_PROPERTY);
        if (runtime == null) {
            throw new IllegalStateException("No AgRuntime found in JAX-RS environment for property: " + JAXRS_AGREST_RUNTIME_PROPERTY);
        }

        return runtime;
    }

    public AgJaxrsFeature(
            AgRuntime runtime,
            Collection<Class<?>> providers,
            Collection<Feature> features) {

        this.runtime = runtime;
        this.providers = providers;
        this.features = features;
    }

    @Override
    public boolean configure(FeatureContext context) {

        context.property(JAXRS_AGREST_RUNTIME_PROPERTY, runtime);
        providers.forEach(context::register);
        features.forEach(f -> f.configure(context));

        return true;
    }

    public AgRuntime getRuntime() {
        return runtime;
    }
}
