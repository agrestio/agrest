package com.nhl.link.rest;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.encoder.Encoder;
import com.nhl.link.rest.encoder.ResourceEncoder;
import com.nhl.link.rest.meta.LrResource;

import java.io.IOException;

public class MetadataResponse extends SimpleResponse {

    private Encoder encoder;
    private LrResource resource;

    public MetadataResponse() {
        super(true, null);
        this.encoder = new ResourceEncoder(null);
    }

    public MetadataResponse withEncoder(Encoder encoder) {
        this.encoder = encoder;
        return this;
    }

    public MetadataResponse withResource(LrResource resource) {
        this.resource = resource;
        return this;
    }

    public void writeData(JsonGenerator out) throws IOException {
		encoder.encode(null, resource, out);
	}

}
