package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.MetadataResponse;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.annotation.Resource;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;

@Path("e2")
public class E2Resource {

	@Context
	private Configuration config;

	@GET
	@Resource(type = LinkType.COLLECTION)
	public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(SelectQuery.query(E2.class), uriInfo);
	}

	@GET
	@Path("{id}")
	@Resource(type = LinkType.ITEM)
	public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E2.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse deleteE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).delete(E2.class, id);
	}

	@GET
	@Path("{id}/dummyrel")
	public DataResponse<E3> getE2_Dummyrel(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "dummyrel").uri(uriInfo).select();
	}

	@GET
	@Path("{id}/e3s")
	public DataResponse<E3> getE2_E3s(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo).select();
	}

	@GET
	@Path("constraints/{id}/e3s")
	public DataResponse<E3> getE2_E3s_Constrained(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E3.class, config).parent(E2.class, id, "e3s").uri(uriInfo)
				.constraints(ConstraintsBuilder.idOnly(E3.class)).select();
	}

	@DELETE
	@Path("{id}/{rel}/{tid}")
	public SimpleResponse deleteToMany(@PathParam("id") int id, @PathParam("rel") String relationship,
			@PathParam("tid") int tid) {
		return LinkRest.service(config).unrelate(E2.class, id, relationship, tid);
	}

	@POST
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdateE3s(@PathParam("id") int id, String targetData) {
		return LinkRest.createOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S).includeData()
				.process(targetData);
	}

	@PUT
	@Path("{id}/e3s")
	public DataResponse<E3> createOrUpdate_Idempotent_E3s(@PathParam("id") int id, String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E3.class, config).toManyParent(E2.class, id, E2.E3S).includeData()
				.process(entityData);
	}

	@GET
	@Path("metadata")
	@Resource(entityClass = E2.class, type = LinkType.METADATA)
	public MetadataResponse getMetadata(@Context UriInfo uriInfo) {
		return LinkRest.metadata(E2.class, config)
				.forResource(E2Resource.class)
				.uri(uriInfo)
				.process();
	}
}
