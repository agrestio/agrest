package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.resolver.RootDataResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * @param <T>
 * @since 3.4
 */
public class RootResourceEntity<T> extends ResourceEntity<T> {

    private String applicationBase;
    private List<T> result;

    public RootResourceEntity(AgEntity<T> agEntity) {
        super(agEntity);
        this.result = new ArrayList<>();
    }

    public List<T> getResult() {
        return result;
    }

    /**
     * Returns a sublist of the data collection with "start" and "limit" constraints applied if present.
     *
     * @since 5.0
     */
    public List<T> getDataWindow() {
        return getDataWindow(result);
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
        return getAgEntity().getDataResolver();
    }
}
