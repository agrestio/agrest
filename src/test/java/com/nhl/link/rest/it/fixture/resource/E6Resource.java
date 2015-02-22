package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E6;

@Path("e6")
public class E6Resource {

	@Context
	private Configuration config;

	@GET
	@Path("{id}")
	public DataResponse<E6> getOne(@PathParam("id") String id) {
		return LinkRest.service(config).selectById(E6.class, id);
	}
}
