package io.agrest;

import io.agrest.meta.AgEntity;
import io.agrest.resolver.RootDataResolver;

import java.util.Collections;
import java.util.List;

/**
 * @param <T>
 * @since 3.4
 */
public class RootResourceEntity<T> extends ResourceEntity<T> {

    private List<T> data;

    public RootResourceEntity(AgEntity<T> agEntity) {
        super(agEntity);
        this.data = Collections.emptyList();
    }

    /**
     * Returns a sublist of the data collection with "start" and "limit" constraints applied if present.
     *
     * @since 5.0
     */
    public List<T> getDataWindow() {
        return getDataWindow(data);
    }

    /**
     * @since 5.0
     */
    public List<T> getData() {
        return data;
    }

    /**
     * @since 5.0
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    public RootDataResolver<T> getResolver() {
        return getAgEntity().getDataResolver();
    }
}
