package io.agrest.jaxrs;

import io.agrest.DataResponse;
import io.agrest.EntityUpdate;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.jaxrs.provider.DataResponseWriter;
import io.agrest.jaxrs.provider.EntityUpdateCollectionReader;
import io.agrest.jaxrs.provider.EntityUpdateReader;
import io.agrest.jaxrs.provider.JaxrsAgExceptionMapper;
import io.agrest.jaxrs.provider.MetadataResponseWriter;
import io.agrest.jaxrs.provider.ResponseStatusDynamicFeature;
import io.agrest.jaxrs.provider.SimpleResponseWriter;
import io.agrest.runtime.AgBuilder;
import io.agrest.runtime.AgRuntime;

import javax.ws.rs.core.Feature;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * @since 5.0
 */
public class AgJaxrsFeatureBuilder {

    private final Map<Class, Class<? extends MessageBodyReader>> bodyReaders;
    private final Map<Class, Class<? extends MessageBodyWriter>> bodyWriters;
    private final List<AgFeatureProvider> featureProviders;
    private final List<Feature> features;
    private boolean autoLoadFeatures;
    private AgRuntime runtime;

    protected AgJaxrsFeatureBuilder() {

        this.autoLoadFeatures = true;
        this.featureProviders = new ArrayList<>(5);
        this.features = new ArrayList<>(5);
        this.bodyReaders = new HashMap<>();
        this.bodyWriters = new HashMap<>();

        loadStandardReaders(bodyReaders);
        loadStandardWriters(bodyWriters);
    }

    protected static void loadStandardReaders(Map<Class, Class<? extends MessageBodyReader>> map) {
        map.put(EntityUpdate.class, EntityUpdateReader.class);
        // TODO: generics-aware key for the reader
        map.put(Collection.class, EntityUpdateCollectionReader.class);
    }

    protected static void loadStandardWriters(Map<Class, Class<? extends MessageBodyWriter>> map) {

        map.put(SimpleResponse.class, SimpleResponseWriter.class);
        map.put(DataResponse.class, DataResponseWriter.class);

        // deprecated writers
        map.put(MetadataResponse.class, MetadataResponseWriter.class);
    }

    public AgJaxrsFeature build() {

        AgRuntime runtime = getOrCreateRuntime();

        List<Class<?>> providers = new ArrayList<>();

        providers.addAll(bodyReaders.values());
        providers.addAll(bodyWriters.values());

        // TODO: care to support multiple custom exception mappers?
        providers.add(JaxrsAgExceptionMapper.class);

        // TODO: care to support multiple custom DynamicFeatures?
        providers.add(ResponseStatusDynamicFeature.class);

        Collection<Feature> features = features(runtime);

        return new AgJaxrsFeature(runtime, providers, features);
    }

    /**
     * Suppresses JAX-RS Feature auto-loading. By default, features are auto-loaded based on the service descriptors
     * under "META-INF/services/io.agrest.jaxrs.AgFeatureProvider". Calling this method would suppress auto-loading
     * behavior, letting the programmer explicitly pick which extensions need to be loaded.
     *
     * @return this builder instance
     */
    public AgJaxrsFeatureBuilder doNotAutoLoadFeatures() {
        this.autoLoadFeatures = false;
        return this;
    }

    /**
     * Registers a JAX-RS feature extending Agrest JAX-RS integration.
     *
     * @param feature a custom JAX-RS feature.
     * @return this builder instance
     */
    public AgJaxrsFeatureBuilder feature(Feature feature) {
        features.add(feature);
        return this;
    }

    /**
     * Registers a provider of a custom JAX-RS feature extending Agrest JAX-RS integration.
     *
     * @param featureProvider a provider of a custom JAX-RS feature.
     * @return this builder instance
     */
    public AgJaxrsFeatureBuilder feature(AgFeatureProvider featureProvider) {
        featureProviders.add(featureProvider);
        return this;
    }

    /**
     * Sets an AgRuntime to be used in JAX-RS environment. If not set, a default runtime is created by the
     * {@link #build()} method.
     *
     * @return this builder instance
     */
    public AgJaxrsFeatureBuilder runtime(AgRuntime runtime) {
        this.runtime = runtime;
        return this;
    }

    private AgRuntime getOrCreateRuntime() {
        return this.runtime != null ? this.runtime : createRuntime();
    }

    private AgRuntime createRuntime() {
        return new AgBuilder().build();
    }

    private List<Feature> features(AgRuntime runtime) {

        List<Feature> features = new ArrayList<>();

        if (autoLoadFeatures) {
            ServiceLoader.load(AgFeatureProvider.class).forEach(fp -> features.add(fp.feature(runtime)));
        }

        features.addAll(this.features);
        featureProviders.forEach(fp -> features.add(fp.feature(runtime)));

        return features;
    }
}
