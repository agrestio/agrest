package io.agrest;

import io.agrest.runtime.AgRESTBuilder;
import org.apache.cayenne.di.Injector;

import javax.ws.rs.core.Feature;

/**
 * A provider of a custom JAX-RS Feature that will be loaded in the JAX-RS runtime as a part of AgREST startup.
 * This object is either directly with {@link AgRESTBuilder} or is auto-loaded via
 * {@link java.util.ServiceLoader} mechanism.
 */
public interface AgFeatureProvider {

    Feature feature(Injector injector);
}
