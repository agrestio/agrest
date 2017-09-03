package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.annotation.LinkType;
import com.nhl.link.rest.annotation.LrResource;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("e2")
public class E2Resource {

	@Context
	private Configuration config;

	@GET
	@LrResource(type = LinkType.COLLECTION)
	public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E2.class).uri(uriInfo).get();
	}

	@GET
	@Path("{id}")
	@LrResource(type = LinkType.ITEM)
	public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E2.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).delete(E2.class, id);
	}

	@DELETE
	public SimpleResponse deleteE2_Batch(Collection<EntityDelete<E2>> deleted, @Context UriInfo uriInfo) {
		return LinkRest.service(config).delete(E2.class, deleted);
	}

	@GET
	@Path("{id}/dummyrel")
	public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "dummyrel").uri(uriInfo).get();
	}

	@GET
	@Path("{id}/e3s")
	public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo).get();
	}

	@GET
	@Path("constraints/{id}/e3s")
	public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo)
				.constraint(Constraint.idOnly(E3.class)).get();
	}

	@DELETE
	@Path("{id}/{rel}/{tid}")
	public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
			@PathParam("tid") int tid) {
		return LinkRest.service(config).unrelate(E2.class, id, relationship, tid);
	}

	@POST
	public DataResponse<E2> createE2(String targetData, @Context UriInfo uriInfo) {
		return LinkRest.create(E2.class, config).uri(uriInfo).syncAndSelect(targetData);
	}

	@POST
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdateE3s(@PathParam("id") int id, String targetData) {
		return LinkRest.createOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S).syncAndSelect(targetData);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E2> createOrUpdate_E2(@PathParam("id") int id, String entityData, @Context UriInfo uriInfo) {
		return LinkRest.idempotentCreateOrUpdate(E2.class, config).id(id).uri(uriInfo).syncAndSelect(entityData);
	}

	@PUT
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S)
				.syncAndSelect(entityData);
	}

	@GET
	@Path("metadata")
	@LrResource(entityClass = E2.class, type = LinkType.METADATA)
	public MetadataResponse<E2> getMetadata(@Context UriInfo uriInfo) {
		return LinkRest.metadata(E2.class, config).forResource(E2Resource.class).uri(uriInfo).process();
	}
}
