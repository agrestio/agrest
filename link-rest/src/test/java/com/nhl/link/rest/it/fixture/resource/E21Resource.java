package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E21;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.HashMap;
import java.util.Map;

@Path("e21")
public class E21Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E21> getE21(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E21.class).uri(uriInfo).select();
	}

	@GET
	@Path("byid")
	public DataResponse<E21> getE21ById(@QueryParam("age") int age, @QueryParam("name") String name,
										@Context UriInfo uriInfo) {
		Map<String, Object> id = new HashMap<>(3);
		id.put("age", age);
		id.put("name", name);
		return LinkRest.service(config).select(E21.class).byId(id).uri(uriInfo).selectOne();
	}

	@DELETE
	@Path("byid")
	public SimpleResponse deleteE21ById(@QueryParam("age") int age, @QueryParam("name") String name) {
		Map<String, Object> id = new HashMap<>(3);
		id.put("age", age);
		id.put("name", name);
		return LinkRest.service(config).delete(E21.class).id(id).delete();
	}

	@POST
	public DataResponse<E21> createE21(EntityUpdate<E21> update, @Context UriInfo uriInfo) {
		return LinkRest.create(E21.class, config).uri(uriInfo).syncAndSelect(update);
	}

	@PUT
	@Path("byid")
	public DataResponse<E21> createOrUpdate_E21(@QueryParam("age") int age, @QueryParam("name") String name,
												EntityUpdate<E21> update, @Context UriInfo uriInfo) {
		Map<String, Object> id = new HashMap<>(3);
		id.put("age", age);
		id.put("name", name);
		return LinkRest.idempotentCreateOrUpdate(E21.class, config).id(id).uri(uriInfo).syncAndSelect(update);
	}
}
