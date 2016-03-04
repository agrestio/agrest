package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E19;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.annotation.Resource;

import javax.ws.rs.GET;
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
		return LinkRest.select(E19.class, config).uri(uriInfo).byId(id).selectOne();
	}

	@GET
	@Path("metadata")
	@Resource(entityClass = E19.class, type = LinkType.METADATA)
	public MetadataResponse<E19> getMetadata(@Context UriInfo uriInfo) {
		return LinkRest.metadata(E19.class, config)
				.forResource(E19Resource.class)
				.uri(uriInfo)
				.process();
	}
}
