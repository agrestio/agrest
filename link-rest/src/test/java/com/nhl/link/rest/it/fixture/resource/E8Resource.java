package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.it.fixture.cayenne.E7;
import com.nhl.link.rest.it.fixture.cayenne.E8;
import com.nhl.link.rest.it.fixture.cayenne.E9;
import com.nhl.link.rest.runtime.cayenne.ByKeyObjectMapperFactory;

@Path("e8")
public class E8Resource {

	@Context
	private Configuration config;

	@PUT
	public DataResponse<E8> sync(@Context UriInfo uriInfo, String data) {
		return LinkRest.idempotentFullSync(E8.class, config).uri(uriInfo).syncAndSelect(data);
	}

	@POST
	@Path("w/constrainedid/{id}")
	public SimpleResponse create_WriteConstrainedId(@PathParam("id") int id, @Context UriInfo uriInfo,
			String requestBody) {
		ConstraintsBuilder<E8> tc = ConstraintsBuilder.idOnly(E8.class).attribute(E8.NAME);
		return LinkRest.create(E8.class, config).uri(uriInfo).id(id).writeConstraints(tc).sync(requestBody);
	}

	@POST
	@Path("w/constrainedidblocked/{id}")
	public DataResponse<E8> create_WriteConstrainedIdBlocked(@PathParam("id") int id, @Context UriInfo uriInfo,
			String requestBody) {
		ConstraintsBuilder<E8> tc = ConstraintsBuilder.excludeAll(E8.class).attribute(E8.NAME);
		return LinkRest.create(E8.class, config).uri(uriInfo).id(id).writeConstraints(tc).syncAndSelect(requestBody);
	}

	@DELETE
	@Path("{id}/e7s")
	public SimpleResponse deleteE7s(@PathParam("id") int id, String entityData) {
		return LinkRest.delete(E7.class, config).toManyParent(E8.class, id, E8.E7S).delete();
	}

	@PUT
	@Path("{id}/e9")
	public DataResponse<E9> relateToOneDependent(@PathParam("id") int id, String entityData) {
		// this will test support for ID propagation in a 1..1
		return LinkRest.idempotentCreateOrUpdate(E9.class, config).parent(E8.class, id, E8.E9)
				.syncAndSelect(entityData);
	}

	@PUT
	@Path("createorupdate/{id}/e7s")
	public DataResponse<E7> createOrUpdateE7s(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E7.class, config).toManyParent(E8.class, id, E8.E7S)
				.syncAndSelect(entityData);
	}

	@PUT
	@Path("{id}/e7s")
	public DataResponse<E7> fullSyncE7s(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentFullSync(E7.class, config).toManyParent(E8.class, id, E8.E7S)
				.syncAndSelect(entityData);
	}

	@PUT
	@Path("bykey/{id}/e7s")
	public DataResponse<E7> e8CreateOrUpdateE7sByKey_Idempotent(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E7.class, config).mapper(ByKeyObjectMapperFactory.byKey(E7.NAME))
				.toManyParent(E8.class, id, E8.E7S).syncAndSelect(entityData);
	}

	@PUT
	@Path("bypropkey/{id}/e7s")
	public DataResponse<E7> e8CreateOrUpdateE7sByPropKey_Idempotent(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E7.class, config).mapper(E7.NAME).toManyParent(E8.class, id, E8.E7S)
				.syncAndSelect(entityData);
	}
}
