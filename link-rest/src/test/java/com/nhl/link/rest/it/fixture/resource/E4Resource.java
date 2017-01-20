package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.listener.CayennePaginationListener;
import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static com.nhl.link.rest.property.PropertyBuilder.property;

@Path("e4")
public class E4Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E4> get(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E4.class).uri(uriInfo).select();
	}

	@GET
	@Path("pagination_listener")
	public DataResponse<E4> get_WithPaginationListener(@Context UriInfo uriInfo) {
		return LinkRest.service(config).select(E4.class).uri(uriInfo).listener(new CayennePaginationListener())
				.select();
	}

	@GET
	@Path("limit_attributes")
	public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
		return LinkRest.select(E4.class, config).uri(uriInfo)
				.constraint(Constraint.idOnly(E4.class).attributes(E4.C_INT))
				.select();
	}

	@GET
	@Path("calc_property")
	public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {

		PropertyReader xReader = new PropertyReader() {

			@Override
			public Object value(Object root, String name) {
				return "y_" + Cayenne.intPKForObject((DataObject) root);
			}
		};

		return LinkRest.select(E4.class, config).uri(uriInfo).property("x", property(xReader)).select();
	}

	@GET
	@Path("{id}")
	public DataResponse<E4> getById(@PathParam("id") int id) {
		return LinkRest.service(config).selectById(E4.class, id);
	}

	@POST
	public DataResponse<E4> create(String requestBody) {
		return LinkRest.create(E4.class, config).syncAndSelect(requestBody);
	}

	@POST
	@Path("defaultdata")
	public SimpleResponse create_DefaultData(String requestBody) {
		return LinkRest.create(E4.class, config).sync(requestBody);
	}

	@GET
	@Path("ie/{id}")
	public DataResponse<E4> get_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
		return LinkRest.service(config).selectById(E4.class, id, uriInfo);
	}

	@DELETE
	@Path("{id}")
	public SimpleResponse delete(@PathParam("id") int id) {
		return LinkRest.service(config).delete(E4.class, id);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E4> update(@PathParam("id") int id, String requestBody) {
		return LinkRest.update(E4.class, config).id(id).syncAndSelect(requestBody);
	}

}
