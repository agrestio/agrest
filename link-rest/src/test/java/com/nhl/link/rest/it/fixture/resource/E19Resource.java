package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.annotation.LinkType;
import com.nhl.link.rest.annotation.LrResource;
import com.nhl.link.rest.it.fixture.cayenne.E19;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("e19")
public class E19Resource {

    @Context
	private Configuration config;

    @GET
    @Path("{id}")
	public DataResponse<E19> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
		return LinkRest.select(E19.class, config).uri(uriInfo).byId(id).getOne();
	}

	@POST
	public DataResponse<E19> create(@Context UriInfo uriInfo, String data) {
		return LinkRest.create(E19.class, config).uri(uriInfo).syncAndSelect(data);
	}

	@POST
	@Path("float")
	public DataResponse<E19> create_FloatAttribute(@Context UriInfo uriInfo, String data) {
		DataResponse<E19> response = LinkRest.create(E19.class, config).uri(uriInfo).syncAndSelect(data);

		int objectCount = response.getObjects().size();
		if (objectCount > 1) {
			throw new IllegalStateException("unexpected number of objects: " + objectCount);
		}
		E19 e19 = response.getObjects().get(0);
		// trigger type casts
		e19.getFloatObject();
		e19.getFloatPrimitive();

		return response;
	}

	@GET
	@Path("metadata")
	@LrResource(entityClass = E19.class, type = LinkType.METADATA)
	public MetadataResponse<E19> getMetadata(@Context UriInfo uriInfo) {
		return LinkRest.metadata(E19.class, config)
				.forResource(E19Resource.class)
				.uri(uriInfo)
				.process();
	}
}
