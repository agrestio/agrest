package com.nhl.link.rest.it.fixture;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E6;

@Path("e6")
public class E6Resource extends LrResource {

	@GET
	@Path("{id}")
	public DataResponse<E6> getOne(@PathParam("id") String id) {
		return getService().selectById(E6.class, id);
	}
}
