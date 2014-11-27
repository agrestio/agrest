package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.it.fixture.cayenne.E14;

@Path("e14")
public class E14Resource extends LrResource {

	@PUT
	public DataResponse<E14> relateToOneExisting(String data) {
		return getService().idempotentFullSync(E14.class).includeData().process(data);
	}
}
