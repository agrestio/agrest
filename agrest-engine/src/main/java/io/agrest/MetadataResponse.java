package io.agrest;

import com.fasterxml.jackson.core.JsonGenerator;
import io.agrest.encoder.Encoder;
import io.agrest.encoder.GenericEncoder;
import io.agrest.meta.AgResource;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * A response object that represents a 'Metadata Document' from Agrest protocol.
 * 
 * @since 1.18
 * @deprecated since 4.1, as Agrest now integrates with OpenAPI 3 / Swagger.
 */
@Deprecated
public class MetadataResponse<T> extends AgResponse {

	private final Class<T> type;
	private Encoder encoder;
	private Collection<AgResource<T>> resources;

	public MetadataResponse(Class<T> type) {
		this.encoder = GenericEncoder.encoder();
		this.type = type;
		this.resources = Collections.emptyList();
	}

	public Class<T> getType() {
		return type;
	}

	/**
	 * @since 1.24
	 */
	public void setEncoder(Encoder encoder) {
		this.encoder = encoder;
	}

	/**
	 * @since 1.24
	 */
	public void setResources(Collection<AgResource<T>> resources) {
		this.resources = resources;
	}

	/**
	 * @since 1.24
	 */
	public void setResource(AgResource<T> resource) {
		setResources(Collections.singletonList(resource));
	}

	public void writeData(JsonGenerator out) throws IOException {
		encoder.encode(null, resources, out);
	}
}
