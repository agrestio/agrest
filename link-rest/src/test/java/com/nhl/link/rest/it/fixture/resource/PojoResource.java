package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.pojo.model.P1;
import com.nhl.link.rest.it.fixture.pojo.model.P6;

@Path("pojo")
public class PojoResource {

	@Context
	private Configuration config;
	
	@GET
	@Path("p1")
	public DataResponse<P1> p1All(@Context UriInfo uriInfo) {
		return LinkRest.select(P1.class, config).uri(uriInfo).select();
	}

	@GET
	@Path("p6")
	public DataResponse<P6> p6All(@Context UriInfo uriInfo) {
		return LinkRest.select(P6.class, config).uri(uriInfo).select();
	}

	@GET
	@Path("p6/{id}")
	public DataResponse<P6> p6ById(@PathParam("id") String id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(P6.class, id, uriInfo);
	}
}
