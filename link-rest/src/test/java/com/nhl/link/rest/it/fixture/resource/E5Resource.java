package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.annotation.Resource;

@Path("e5")
public class E5Resource {

	@Context
	private Configuration config;

	@GET
	@Resource(type = LinkType.COLLECTION)
	public DataResponse<E5> getE5(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E5.class).uri(uriInfo).select();
	}

	@GET
	@Path("{id}")
	@Resource(type = LinkType.ITEM)
	public DataResponse<E5> getE5ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E5.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse deleteE5ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).delete(E5.class, id);
	}

	@GET
	@Path("metadata")
	@Resource(entityClass = E5.class, type = LinkType.METADATA)
	public MetadataResponse<E5> getMetadata(@Context UriInfo uriInfo) {
		return LinkRest.metadata(E5.class, config).forResource(E5Resource.class).uri(uriInfo).process();
	}

}
