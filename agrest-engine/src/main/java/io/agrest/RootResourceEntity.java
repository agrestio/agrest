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

    @Deprecated
    private String applicationBase;
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
     * @deprecated in favor of {@link #getData()}
     */
    @Deprecated(since = "5.0")
    public List<T> getResult() {
        return data;
    }

    /**
     * @since 5.0
     */
    public void setData(List<T> data) {
        this.data = data;
    }

    /**
     * @deprecated in favor of {@link #setData(List)}
     */
    @Deprecated(since = "5.0")
    public void setResult(List<T> data) {
        this.data = data;
    }

    /**
     * @since 1.20
     * @deprecated as metadata encoding that used this was removed from Agrest
     */
    @Deprecated(since = "5.0")
    public String getApplicationBase() {
        return applicationBase;
    }

    /**
     * @since 1.20
     * @deprecated as metadata encoding that used this was removed from Agrest
     */
    @Deprecated(since = "5.0")
    public void setApplicationBase(String applicationBase) {
        this.applicationBase = applicationBase;
    }

    public RootDataResolver<T> getResolver() {
        return getAgEntity().getDataResolver();
    }
}
