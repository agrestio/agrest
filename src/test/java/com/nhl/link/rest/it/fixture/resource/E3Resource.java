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

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.constraints.ConstraintsBuilder;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;

@Path("e3")
public class E3Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E3> get(@Context UriInfo uriInfo) {
		SelectQuery<E3> query = new SelectQuery<E3>(E3.class);
		return LinkRest.service(config).select(query, uriInfo);
	}

	@POST
	public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
		return LinkRest.create(E3.class, config).with(uriInfo).includeData().process(requestBody);
	}

	@PUT
	public DataResponse<E3> sync(@Context UriInfo uriInfo, String requestBody) {
		return LinkRest.idempotentFullSync(E3.class, config).with(uriInfo).includeData().process(requestBody);
	}

	@GET
	@Path("{id}")
	public DataResponse<E3> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E3.class, id, uriInfo);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
		return LinkRest.update(E3.class, config).id(id).includeData().process(requestBody);
	}

	@POST
	@Path("constrained")
	public DataResponse<E3> insertReadConstrained(@Context UriInfo uriInfo, String requestBody) {
		ConstraintsBuilder<E3> tc = ConstraintsBuilder.idOnly(E3.class).attribute(E3.NAME);
		return LinkRest.create(E3.class, config).with(uriInfo).readConstraints(tc).includeData().process(requestBody);
	}

	@POST
	@Path("w/constrained")
	public DataResponse<E3> insertWriteConstrained(@Context UriInfo uriInfo, String requestBody) {
		ConstraintsBuilder<E3> tc = ConstraintsBuilder.idOnly(E3.class).attribute(E3.NAME);
		return LinkRest.create(E3.class, config).with(uriInfo).writeConstraints(tc).includeData().process(requestBody);
	}

	@GET
	@Path("{id}/e2")
	public DataResponse<E2> getE2(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.select(E2.class, config).parent(E3.class, id, E3.E2).with(uriInfo).select();
	}

	@DELETE
	@Path("{id}/e2")
	public SimpleResponse deleteE2_Implicit(@PathParam("id") int id) {
		return LinkRest.service(config).unrelate(E3.class, id, E3.E2);
	}

	@PUT
	@Path("{id}/e2/{tid}")
	public DataResponse<E2> createOrUpdate_Idempotent_E2(@PathParam("id") int parentId, @PathParam("tid") int id,
			String entityData) {
		return LinkRest.idempotentCreateOrUpdate(E2.class, config).id(id).includeData()
				.parent(E3.class, parentId, E3.E2).process(entityData);
	}

	@DELETE
	@Path("{id}/e2/{tid}")
	public SimpleResponse deleteE2(@PathParam("id") int id, @PathParam("tid") int tid) {
		return LinkRest.service(config).unrelate(E3.class, id, E3.E2, tid);
	}
}
