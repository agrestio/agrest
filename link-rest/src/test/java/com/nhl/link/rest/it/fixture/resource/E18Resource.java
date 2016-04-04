package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E18;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("e18")
public class E18Resource {

    @Context
	private Configuration config;

    @GET
    @Path("{id}")
	public DataResponse<E18> getById(@Context UriInfo uriInfo, @PathParam("id") Integer id) {
		return LinkRest.select(E18.class, config).uri(uriInfo).byId(id).selectOne();
	}
}
