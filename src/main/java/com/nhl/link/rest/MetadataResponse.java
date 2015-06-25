package com.nhl.link.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.GenericEncoder;
import com.nhl.link.rest.meta.LrResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class MetadataResponse<T> extends SimpleResponse {

    private Encoder encoder;
    private Collection<LrResource> resources;
    private Class<T> type;
    private ResourceEntity<T> entity;
    private String applicationBase;

    public MetadataResponse(Class<T> type) {
        super(true, null);
        this.encoder = GenericEncoder.encoder();
        this.type = type;
        this.resources = new ArrayList<>();
    }

    public MetadataResponse<T> resourceEntity(ResourceEntity<T> entity) {
        this.entity = entity;
        return this;
    }

    public MetadataResponse<T> withApplicationBase(String applicationBase) {
        this.applicationBase = applicationBase;
        return this;
    }

    public MetadataResponse<T> withEncoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public MetadataResponse<T> withResources(Collection<LrResource> resources) {
        this.resources.addAll(resources);
        return this;
    }

    public MetadataResponse<T> withResource(LrResource resource) {
        this.resources.add(resource);
        return this;
    }

    public void writeData(JsonGenerator out) throws IOException {
		encoder.encode(null, resources, out);
	}

    public ResourceEntity<T> getEntity() {
        return entity;
    }

    public String getApplicationBase() {
        return applicationBase;
    }

}
