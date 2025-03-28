package io.agrest.jaxrs2;

import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Feature;

/**
 * A provider of a custom JAX-RS Feature that will be loaded in the JAX-RS runtime as a part of Agrest startup.
 * This object is either installed directly via {@link AgJaxrsFeatureBuilder#feature(AgFeatureProvider)} or is
 * referenced in "META-INF/services/io.agrest.jaxrs2.AgFeatureProvider" to be auto-loaded via
 * {@link java.util.ServiceLoader}.
 *
 * @deprecated in favor of Jakarta version (JAX-RS 3)
 */
@Deprecated(since = "5.0", forRemoval = true)
public interface AgFeatureProvider {

    /**
     * Creates and returns a JAX-RS Feature for the Agrest runtime argument.
     */
    Feature feature(AgRuntime runtime);
}
