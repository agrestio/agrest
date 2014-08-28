package com.nhl.link.rest.unit.resource;

import static com.nhl.link.rest.TreeConstraints.idOnly;
import static com.nhl.link.rest.property.PropertyBuilder.property;

import java.io.IOException;

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

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.property.PropertyReader;
import com.nhl.link.rest.unit.cayenne.E4;

@Path("e4")
public class E4Resource extends LrResource {

	@GET
	public DataResponse<E4> get(@Context UriInfo uriInfo) {
		return getService().select(SelectQuery.query(E4.class), uriInfo);
	}

	@GET
	@Path("limit_attributes")
	public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {
		return getService().forSelect(E4.class).constraints(idOnly(E4.class).attributes(E4.C_INT)).select();
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
	@Path("manual_property")
	public DataResponse<E4> property_Object(@Context UriInfo uriInfo) {

		SelectQuery<E4> query = new SelectQuery<E4>(E4.class);

		EntityProperty xProperty = new EntityProperty() {

			@Override
			public void encode(Object root, String propertyName, JsonGenerator out) throws IOException {
				out.writeStartObject();

				out.writeStringField("f1", "y_" + Cayenne.intPKForObject((DataObject) root));
				out.writeNumberField("f2", 1.15d);

				out.writeEndObject();
			}
		};

		return getService().forSelect(query).with(uriInfo).withProperty("x", xProperty).select();
	}

	@GET
	@Path("{id}")
	public DataResponse<E4> getById(@PathParam("id") int id) {
		return getService().selectById(E4.class, id);
	}

	@POST
	public DataResponse<E4> create(String requestBody) {
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
		return getService().update(E4.class, id, requestBody);
	}

}
