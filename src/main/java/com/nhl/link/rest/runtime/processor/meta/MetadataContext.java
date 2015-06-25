package com.nhl.link.rest.runtime.processor.meta;

import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.processor.BaseProcessingContext;

import javax.ws.rs.core.UriInfo;

public class MetadataContext<T> extends BaseProcessingContext<T> {

    private Class<?> resourceClass;
    private UriInfo uriInfo;
    private MetadataResponse response;
    private LrEntity<T> entity;

    public MetadataContext(Class<T> type) {
        super(type);
    }

    public LrEntity<T> getEntity() {
        return entity;
    }

    public void setEntity(LrEntity<T> entity) {
        this.entity = entity;
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
