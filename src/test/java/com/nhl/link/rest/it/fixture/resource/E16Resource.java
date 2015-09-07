package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E16;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

@Path("e16")
public class E16Resource {

    @Context
	private Configuration config;

    @POST
	public DataResponse<E16> create(String requestBody) {
		return LinkRest.create(E16.class, config).syncAndSelect(requestBody);
	}

}
