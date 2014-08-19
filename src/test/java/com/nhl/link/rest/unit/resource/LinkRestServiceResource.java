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

import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E2;
import com.nhl.link.rest.unit.cayenne.E3;
import com.nhl.link.rest.unit.cayenne.E4;

@Path("lr")
public class LinkRestServiceResource {

	@Context
	private Configuration config;

	private ILinkRestService getLinkRestService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	@GET
	@Path("all")
	public DataResponse<E4> getObjects(@Context UriInfo uriInfo) {
		SelectQuery<E4> query = new SelectQuery<E4>(E4.class);
		return getLinkRestService().select(query, uriInfo);
	}

	@GET
	@Path("{id}")
	public DataResponse<E4> getObject(@PathParam("id") int id) {
		return getLinkRestService().selectById(E4.class, id);
	}

	@GET
	@Path("ie/{id}")
	public DataResponse<E4> getObjectWithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getLinkRestService().selectById(E4.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse deleteObjects(@PathParam("id") int id) {
		return getLinkRestService().delete(E4.class, id);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E4> updateE4(@PathParam("id") int id, String requestBody) {
		return getLinkRestService().update(E4.class, id, requestBody);
	}

	@POST
	public DataResponse<E4> insertE4(String requestBody) {
		return getLinkRestService().create(E4.class).process(requestBody);
	}

	@PUT
	@Path("e3/{id}")
	public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
		return getLinkRestService().update(E3.class, id, requestBody);
	}

	@POST
	@Path("e3")
	public DataResponse<E3> insertE3(String requestBody) {
		return getLinkRestService().create(E3.class).process(requestBody);
	}

	@POST
	@Path("constrained/e3")
	public DataResponse<E3> insertConstrainedE3(@Context UriInfo uriInfo, String requestBody) {
		TreeConstraints tc = TreeConstraints.idOnly().attribute(E3.NAME);
		return getLinkRestService().create(E3.class).with(uriInfo).readConstraints(tc).process(requestBody);
	}

	@GET
	@Path("e3/{id}")
	public DataResponse<E3> getE3ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getLinkRestService().selectById(E3.class, id, uriInfo);
	}

	@GET
	@Path("e2/{id}")
	public DataResponse<E2> getE2ById(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getLinkRestService().selectById(E2.class, id, uriInfo);
	}

	@GET
	@Path("e2")
	public DataResponse<E2> getE2(@Context UriInfo uriInfo) {
		return getLinkRestService().select(SelectQuery.query(E2.class), uriInfo);
	}

	@GET
	@Path("e3")
	public DataResponse<E3> getE3s(@Context UriInfo uriInfo) {
		SelectQuery<E3> query = new SelectQuery<E3>(E3.class);
		return getLinkRestService().select(query, uriInfo);
	}

	@GET
	@Path("e4")
	public DataResponse<E4> getE4(@Context UriInfo uriInfo) {
		return getLinkRestService().select(SelectQuery.query(E4.class), uriInfo);
	}
}
