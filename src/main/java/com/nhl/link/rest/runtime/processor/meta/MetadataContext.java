package com.nhl.link.rest.runtime.processor.meta;

import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;

public class MetadataContext<T> extends BaseProcessingContext<T> {

    private Class<?> resourceClass;
    private UriInfo uriInfo;
    private MetadataResponse response;
    private String path;

    public MetadataContext(Class<T> type) {
        super(type);
    }

    public void setResource(Class<?> resourceClass) {
        this.resourceClass = resourceClass;
    }

    public Class<?> getResource() {
        return resourceClass;
    }

    public MetadataResponse getResponse() {
        return response;
    }

    public void setResponse(MetadataResponse response) {
        this.response = response;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public UriInfo getUriInfo() {
        return uriInfo;
    }

    public void setUriInfo(UriInfo uriInfo) {
        this.uriInfo = uriInfo;
    }

    public String getApplicationBase() {
        if (uriInfo == null) {
            return null;
        }
        return uriInfo.getBaseUri().toString();
    }
}
