package com.nhl.link.rest.unit.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.runtime.cayenne.ByKeyAndParentObjectMapper;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.cayenne.E7;
import com.nhl.link.rest.unit.cayenne.E8;
import com.nhl.link.rest.unit.cayenne.E9;

@Path("lr/related")
public class LinkRestResource_Related {

	@Context
	private Configuration config;

	private ILinkRestService getService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	@GET
	@Path("e2/{id}/dummyrel")
	public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().forSelect(E3.class).parent(E2.class, id, "dummyrel").with(uriInfo).select();
	}

	@GET
	@Path("e2/{id}/e3s")
	public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return (DataResponse<E3>) getService().forSelect(E3.class).parent(E2.class, id, "e3s").with(uriInfo).select();
	}

	@GET
	@Path("constraints/e2/{id}/e3s")
	public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return (DataResponse<E3>) getService().forSelect(E3.class).parent(E2.class, id, "e3s").with(uriInfo)
				.constraints(TreeConstraints.idOnly()).select();
	}

	@GET
	@Path("e3/{id}/e2")
	public DataResponse<E2> getE3_E2(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().forSelect(E2.class).parent(E3.class, id, E3.E2).with(uriInfo).select();
	}

	@DELETE
	@Path("e2/{id}/{rel}/{tid}")
	public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
			@PathParam("tid") int tid) {
		return getService().unrelate(E2.class, id, relationship, tid);
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
	@Path("e2/{id}/e3s")
	public DataResponse<E3> e3_Related_CreateOrUpdate(@PathParam("id") int id, String targetData) {
		return getService().createOrUpdate(E3.class).toManyParent(E2.class, id, E2.E3S).process(targetData);
	}

	@PUT
	@Path("e2/{id}/e3s")
	public DataResponse<E3> e3_Related_CreateOrUpdate_Idempotent(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E3.class).toManyParent(E2.class, id, E2.E3S).process(entityData);
	}

	@PUT
	@Path("e3/{id}/e2/{tid}")
	public DataResponse<E2> e2_Related_CreateOrUpdate_Idempotent(@PathParam("id") int parentId,
			@PathParam("tid") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E2.class).id(id).parent(E3.class, parentId, E3.E2)
				.process(entityData);
	}

	@PUT
	@Path("e7/{id}/e8/{tid}")
	public DataResponse<E8> e7RelateToOneExisting(@PathParam("id") int parentId, @PathParam("tid") int id, String data) {
		return getService().idempotentCreateOrUpdate(E8.class).id(id).parent(E7.class, parentId, E7.E8).process(data);
	}

	@DELETE
	@Path("e8/{id}/e7s")
	public SimpleResponse e8DeleteE7s(@PathParam("id") int id, String entityData) {
		return getService().delete(E7.class).toManyParent(E8.class, id, E8.E7S).delete();
	}

	@PUT
	@Path("e8/{id}/e9")
	public DataResponse<E9> e8RelateToOneDependent(@PathParam("id") int id, String entityData) {
		// this will test support for ID propagation in a 1..1
		return getService().idempotentCreateOrUpdate(E9.class).parent(E8.class, id, E8.E9).process(entityData);
	}

	@PUT
	@Path("e8/{id}/e7s")
	public DataResponse<E7> e8CreateOrUpdateE7s(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E7.class).toManyParent(E8.class, id, E8.E7S).process(entityData);
	}

	@PUT
	@Path("bykey/e8/{id}/e7s")
	public DataResponse<E7> e8CreateOrUpdateE7sByKey_Idempotent(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E7.class)
				.mapper(ByKeyAndParentObjectMapper.byKeyAndParent(E7.NAME)).toManyParent(E8.class, id, E8.E7S)
				.process(entityData);
	}
}
