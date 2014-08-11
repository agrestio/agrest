package com.nhl.link.rest.unit.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

@Path("lr/related")
public class LinkRestResource_Related {

	@Context
	private Configuration config;

	private ILinkRestService getService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	/**
	 * A generic relationship method that can read any relationship of the E2
	 * entity.
	 */
	@GET
	@Path("e2/{id}/{rel}")
	public DataResponse<?> getAnyRelationship(@PathParam("id") int id, @PathParam("rel") String relationship,
			@Context UriInfo uriInfo) {
		return getService().forSelectRelated(E2.class, id, relationship).with(uriInfo).select();
	}

	@DELETE
	@Path("e2/{id}/{rel}/{tid}")
	public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
			@PathParam("tid") int tid) {
		return getService().unrelate(E2.class, id, relationship, tid);
	}

	@POST
	@Path("e2/{id}/{rel}")
	public SimpleResponse relateToMany(@PathParam("id") int id, @PathParam("rel") String relationship, String targetData) {
		return getService().relateNew(E2.class, id, relationship, targetData);
	}

	/**
	 * A specific relationship method that reads e2.e3 to-one relationship.
	 */
	@GET
	@Path("e3/{id}/e2")
	public DataResponse<?> getSpecificRelationship(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().forSelectRelated(E3.class, id, E3.E2).with(uriInfo).select();
	}

	@DELETE
	@Path("e3/{id}/e2/{tid}")
	public SimpleResponse deleteToOne(@PathParam("id") int id, @PathParam("tid") int tid) {
		return getService().unrelate(E3.class, id, E3.E2, tid);
	}

	@DELETE
	@Path("e3/{id}/e2")
	public SimpleResponse deleteToOne_Implicit(@PathParam("id") int id) {
		return getService().unrelate(E3.class, id, E3.E2);
	}

	@POST
	@Path("e3/{id}/e2/{tid}")
	public SimpleResponse relateToOneExisting(@PathParam("id") int id, @PathParam("tid") int tid) {
		return getService().relate(E3.class, id, E3.E2, tid);
	}
}
