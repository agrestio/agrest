package com.nhl.link.rest.it.fixture;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E12;
import com.nhl.link.rest.it.fixture.cayenne.E12E13;

@Path("e12")
public class E12Resource extends LrResource {

	@GET
	@Path("{id}/e1213")
	public DataResponse<E12E13> get_Joins_NoId(@PathParam("id") int id, @Context UriInfo info) {
		return getService().forSelect(E12E13.class).toManyParent(E12.class, id, E12.E1213).with(info).select();
	}

	@POST
	@Path("{id}/e1213")
	public DataResponse<E12E13> create_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
		return getService().create(E12E13.class).toManyParent(E12.class, id, E12.E1213).with(info).includeData()
				.process(entityData);
	}

	@PUT
	@Path("{id}/e1213")
	public DataResponse<E12E13> fullSync_Joins(@PathParam("id") int id, @Context UriInfo info, String entityData) {
		return getService().idempotentFullSync(E12E13.class).toManyParent(E12.class, id, E12.E1213).with(info)
				.includeData().process(entityData);
	}
}
