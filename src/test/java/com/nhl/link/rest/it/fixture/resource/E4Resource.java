package com.nhl.link.rest.it.fixture.resource;

import static com.nhl.link.rest.TreeConstraints.idOnly;
import static com.nhl.link.rest.property.PropertyBuilder.property;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.property.PropertyReader;

@Path("e4")
public class E4Resource extends LrResource {

	@GET
	public DataResponse<E4> get(@Context UriInfo uriInfo) {
		return getService().select(SelectQuery.query(E4.class), uriInfo);
	}

	@GET
	@Path("limit_attributes")
	public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
		return getService().forSelect(E4.class).with(uriInfo).constraints(idOnly(E4.class).attributes(E4.C_INT))
				.select();
	}

	@GET
	@Path("calc_property")
	public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {

		SelectQuery<E4> query = new SelectQuery<E4>(E4.class);

		PropertyReader xReader = new PropertyReader() {

			@Override
			public Object value(Object root, String name) {
				return "y_" + Cayenne.intPKForObject((DataObject) root);
			}
		};

		return getService().forSelect(query).with(uriInfo).withProperty("x", property(xReader)).select();
	}

	@GET
	@Path("{id}")
	public DataResponse<E4> getById(@PathParam("id") int id) {
		return getService().selectById(E4.class, id);
	}

	@POST
	public DataResponse<E4> create(String requestBody) {
		return getService().create(E4.class).includeData().process(requestBody);
	}

	@POST
	@Path("defaultdata")
	public DataResponse<E4> create_DefaultData(String requestBody) {
		return getService().create(E4.class).process(requestBody);
	}

	@GET
	@Path("ie/{id}")
	public DataResponse<E4> get_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return getService().selectById(E4.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse delete(@PathParam("id") int id) {
		return getService().delete(E4.class, id);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E4> update(@PathParam("id") int id, String requestBody) {
		return getService().update(E4.class).id(id).includeData().process(requestBody);
	}

}
