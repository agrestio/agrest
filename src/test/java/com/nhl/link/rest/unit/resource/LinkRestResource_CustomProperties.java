package com.nhl.link.rest.unit.resource;

import static com.nhl.link.rest.property.PropertyBuilder.property;

import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.query.SelectQuery;

import com.fasterxml.jackson.core.JsonGenerator;
import com.nhl.link.rest.EntityProperty;
import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.property.PropertyReader;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E4;

@Path("lr/custom_properties")
public class LinkRestResource_CustomProperties {

	@Context
	private Configuration config;

	private ILinkRestService getLinkRestService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	@GET
	@Path("e4/calc_property")
	public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {

		SelectQuery<E4> query = new SelectQuery<E4>(E4.class);

		PropertyReader xReader = new PropertyReader() {

			@Override
			public Object value(Object root, String name) {
				return "y_" + Cayenne.intPKForObject((DataObject) root);
			}
		};

		return getLinkRestService().forSelect(query).with(uriInfo).withProperty("x", property(xReader)).select();
	}

	@GET
	@Path("e4/manual_property")
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

		return getLinkRestService().forSelect(query).with(uriInfo).withProperty("x", xProperty).select();
	}
}
