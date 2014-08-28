package com.nhl.link.rest.unit.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.unit.cayenne.E10;

@Path("/e10")
public class E10Resource extends LrResource {

	@GET
	public DataResponse<E10> get(@Context UriInfo uriInfo) {
		return getService().forSelect(E10.class).with(uriInfo).select();
	}
}
