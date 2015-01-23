package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E14;

@Path("e14")
public class E14Resource extends LrResource {

	@GET
	public DataResponse<E14> get(@Context UriInfo uriInfo) {
		return getService().forSelect(E14.class).with(uriInfo).select();
	}

	@POST
	public DataResponse<E14> post(String data) {
		return getService().create(E14.class).includeData().process(data);
	}

	@PUT
	public DataResponse<E14> sync(String data) {
		return getService().idempotentFullSync(E14.class).includeData().process(data);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E14> update(@PathParam("id") int id, String data) {
		return getService().update(E14.class).id(id).includeData().process(data);
	}

}
