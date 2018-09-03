package io.agrest.runtime.adapter;

import org.apache.cayenne.di.Binder;

import javax.ws.rs.core.Feature;
import java.util.Collection;

/**
 * Defines interface of an "adapter" that customizes default LinkRest runtime
 * for a specific flavor of REST interactions. Adapter interface is very generic
 * and allows to customize all aspects of LinkRest accessible via DI.
 *
 * @since 1.3
 * @deprecated since 2.10 in favor of {@link io.agrest.LrFeatureProvider} and
 * {@link io.agrest.LrModuleProvider}. Either can be registered with
 * {@link io.agrest.runtime.LinkRestBuilder} explicitly or used to implemented auto-loadable extensions.
 */
@Deprecated
public interface LinkRestAdapter {

    void contributeToRuntime(Binder binder);

    void contributeToJaxRs(Collection<Feature> features);
}
