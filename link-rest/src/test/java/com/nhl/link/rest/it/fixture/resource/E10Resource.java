package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E10;

@Path("/e10")
public class E10Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E10> get(@Context UriInfo uriInfo) {
		return LinkRest.select(E10.class, config).uri(uriInfo).get();
	}
	
	
}
