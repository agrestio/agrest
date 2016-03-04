package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E14;

@Path("e14")
public class E14Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E14> get(@Context UriInfo uriInfo) {
		return LinkRest.select(E14.class, config).uri(uriInfo).select();
	}

	@POST
	public DataResponse<E14> post(String data) {
		return LinkRest.create(E14.class, config).syncAndSelect(data);
	}

	@PUT
	public DataResponse<E14> sync(String data) {
		return LinkRest.idempotentFullSync(E14.class, config).syncAndSelect(data);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E14> update(@PathParam("id") int id, String data) {
		return LinkRest.update(E14.class, config).id(id).syncAndSelect(data);
	}

}
