package com.nhl.link.rest.unit.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.unit.pojo.model.P6;

@Path("pojo")
public class PojoResource extends LrResource {

	@GET
	@Path("p6")
	public DataResponse<P6> p6All(@Context UriInfo uriInfo) {
		return getService().forSelect(P6.class).with(uriInfo).select();
	}

	@GET
	@Path("p6/{id}")
	public DataResponse<P6> p6ById(@PathParam("id") String id, @Context UriInfo uriInfo) {
		return getService().selectById(P6.class, id, uriInfo);
	}
}
