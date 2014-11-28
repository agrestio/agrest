package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;
import com.nhl.link.rest.it.fixture.cayenne.E9;
import com.nhl.link.rest.runtime.cayenne.ByKeyObjectMapperFactory;

@Path("e8")
public class E8Resource extends LrResource {
	
	@PUT
	public DataResponse<E8> sync(@Context UriInfo uriInfo, String data) {
		return getService().idempotentFullSync(E8.class).with(uriInfo).includeData().process(data);
	}

	@POST
	@Path("w/constrainedid/{id}")
	public DataResponse<E8> create_WriteConstrainedId(@PathParam("id") int id, @Context UriInfo uriInfo,
			String requestBody) {
		TreeConstraints<E8> tc = TreeConstraints.idOnly(E8.class).attribute(E8.NAME);
		return getService().create(E8.class).with(uriInfo).id(id).writeConstraints(tc).process(requestBody);
	}

	@POST
	@Path("w/constrainedidblocked/{id}")
	public DataResponse<E8> create_WriteConstrainedIdBlocked(@PathParam("id") int id, @Context UriInfo uriInfo,
			String requestBody) {
		TreeConstraints<E8> tc = TreeConstraints.excludeAll(E8.class).attribute(E8.NAME);
		return getService().create(E8.class).with(uriInfo).id(id).writeConstraints(tc).includeData()
				.process(requestBody);
	}

	@DELETE
	@Path("{id}/e7s")
	public SimpleResponse deleteE7s(@PathParam("id") int id, String entityData) {
		return getService().delete(E7.class).toManyParent(E8.class, id, E8.E7S).delete();
	}

	@PUT
	@Path("{id}/e9")
	public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
		// this will test support for ID propagation in a 1..1
		return getService().idempotentCreateOrUpdate(E9.class).parent(E8.class, id, E8.E9).includeData()
				.process(entityData);
	}

	@PUT
	@Path("createorupdate/{id}/e7s")
	public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E7.class).toManyParent(E8.class, id, E8.E7S).includeData()
				.process(entityData);
	}

	@PUT
	@Path("{id}/e7s")
	public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
		return getService().idempotentFullSync(E7.class).toManyParent(E8.class, id, E8.E7S).includeData()
				.process(entityData);
	}

	@PUT
	@Path("bykey/{id}/e7s")
	public DataResponse<E7> e8CreateOrUpdateE7sByKey_Idempotent(@PathParam("id") int id, String entityData) {
		return getService().idempotentCreateOrUpdate(E7.class).mapper(ByKeyObjectMapperFactory.byKey(E7.NAME))
				.toManyParent(E8.class, id, E8.E7S).includeData().process(entityData);
	}
}
