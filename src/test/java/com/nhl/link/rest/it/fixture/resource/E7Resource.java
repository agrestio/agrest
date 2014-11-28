package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;

@Path("e7")
public class E7Resource extends LrResource {

	@PUT
	public DataResponse<E7> sync(@Context UriInfo uriInfo, String data) {
		return getService().idempotentFullSync(E7.class).with(uriInfo).includeData().process(data);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E7> syncOne(@PathParam("id") int id, @Context UriInfo uriInfo, String data) {
		return getService().idempotentFullSync(E7.class).id(id).with(uriInfo).includeData().process(data);
	}

	@PUT
	@Path("{id}/e8/{tid}")
	public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
		return getService().idempotentCreateOrUpdate(E8.class).id(id).includeData().parent(E7.class, parentId, E7.E8)
				.process(data);
	}
}
