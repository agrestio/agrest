package com.nhl.link.rest.unit.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;

@Path("e2")
public class E2Resource extends LrResource {

	@GET
	public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
		return getService().select(SelectQuery.query(E2.class), uriInfo);
	}

	@GET
	@Path("{id}")
	public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().selectById(E2.class, id, uriInfo);
	}

	@GET
	@Path("{id}/dummyrel")
	public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().forSelect(E3.class).parent(E2.class, id, "dummyrel").with(uriInfo).select();
	}

	@GET
	@Path("{id}/e3s")
	public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return (DataResponse<E3>) getService().forSelect(E3.class).parent(E2.class, id, "e3s").with(uriInfo).select();
	}

	@GET
	@Path("constraints/{id}/e3s")
	public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return (DataResponse<E3>) getService().forSelect(E3.class).parent(E2.class, id, "e3s").with(uriInfo)
				.constraints(TreeConstraints.idOnly(E3.class)).select();
	}

	@DELETE
	@Path("{id}/{rel}/{tid}")
	public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
			@PathParam("tid") int tid) {
		return getService().unrelate(E2.class, id, relationship, tid);
	}

	@POST
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdateE3s(@PathParam("id") int id, String targetData) {
		return getService().createOrUpdate(E3.class).toManyParent(E2.class, id, E2.E3S).process(targetData);
	}

	@PUT
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E3.class).toManyParent(E2.class, id, E2.E3S).process(entityData);
	}
}
