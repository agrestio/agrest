package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.resolver.RootDataResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @since 3.4
 */
public class RootResourceEntity<T> extends ResourceEntity<T> {

    private String applicationBase;
    private RootDataResolver<T> resolver;
    private List<T> result;

    public RootResourceEntity(AgEntity<T> agEntity, AgEntityOverlay<T> agEntityOverlay) {
        super(agEntity, agEntityOverlay);
        this.result = new ArrayList<>();
    }

    public List<T> getResult() {
        return result;
    }

    public void setResult(List<T> result) {
        this.result = result;
    }

    /**
     * @since 1.20
     */
    public String getApplicationBase() {
        return applicationBase;
    }

    /**
     * @since 1.20
     */
    public void setApplicationBase(String applicationBase) {
        this.applicationBase = applicationBase;
    }

    public RootDataResolver<T> getResolver() {
        return resolver;
    }

    public void setResolver(RootDataResolver<T> resolver) {
        this.resolver = resolver;
    }
}
