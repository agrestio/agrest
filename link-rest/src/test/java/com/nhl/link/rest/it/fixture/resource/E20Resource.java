package com.nhl.link.rest.it.fixture.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E20;
import com.nhl.link.rest.it.fixture.cayenne.E21;

@Path("e20")
public class E20Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E20> getE20(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E20.class).uri(uriInfo).select();
	}

	@GET
	@Path("{id}")
	public DataResponse<E20> getE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E20.class, name, uriInfo);
	}

	@GET
	@Path("byparent")
	public DataResponse<E20> getE20ByParent(@QueryParam("age") int age, @QueryParam("name") String name,
											@Context UriInfo uriInfo) {

		Map<String, Object> parentId = new HashMap<>(3);
		parentId.put(E21.AGE.getName(), age);
		parentId.put(E21.NAME.getName(), name);
		return LinkRest.service(config).select(E20.class).parent(E21.class, parentId, E21.E20S.getName()).uri(uriInfo).select();
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse deleteE20ById(@PathParam("id") String name, @Context UriInfo uriInfo) {
		return LinkRest.service(config).delete(E20.class, name);
	}

	@POST
	public DataResponse<E20> createE20(EntityUpdate<E20> update, @Context UriInfo uriInfo) {
		return LinkRest.create(E20.class, config).uri(uriInfo).syncAndSelect(update);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E20> createOrUpdate_E20(@PathParam("id") String name, EntityUpdate<E20> update, @Context UriInfo uriInfo) {
		return LinkRest.idempotentCreateOrUpdate(E20.class, config).id(name).uri(uriInfo).syncAndSelect(update);
	}
}
