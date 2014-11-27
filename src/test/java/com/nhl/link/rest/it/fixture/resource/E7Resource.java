package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;

@Path("e7")
public class E7Resource extends LrResource {

	@PUT
	@Path("{id}/e8/{tid}")
	public DataResponse<E8> relateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
		return getService().idempotentCreateOrUpdate(E8.class).id(id).includeData().parent(E7.class, parentId, E7.E8)
				.process(data);
	}
}
