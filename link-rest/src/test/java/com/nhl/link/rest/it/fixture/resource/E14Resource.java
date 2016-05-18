package com.nhl.link.rest.it.fixture.resource;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.apache.cayenne.Cayenne;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.it.fixture.cayenne.E14;
import com.nhl.link.rest.it.fixture.pojo.model.P7;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

@Path("e14")
public class E14Resource {

	@Context
	private Configuration config;

	@GET
	public DataResponse<E14> get(@Context UriInfo uriInfo) {
		return LinkRest.select(E14.class, config).listener(new P7Listener()).uri(uriInfo).select();
	}

	@POST
	public DataResponse<E14> post(String data) {
		return LinkRest.create(E14.class, config).syncAndSelect(data);
	}

	@PUT
	public DataResponse<E14> sync(String data) {
		return LinkRest.idempotentFullSync(E14.class, config).syncAndSelect(data);
	}

	@PUT
	@Path("{id}")
	public DataResponse<E14> update(@PathParam("id") int id, String data) {
		return LinkRest.update(E14.class, config).id(id).syncAndSelect(data);
	}

	public static class P7Listener {

		@DataFetched
		public void afterCayenneFetch(SelectContext<E14> context) {

			for (E14 e14 : context.getEntity().getObjects()) {
				P7 p7 = new P7();
				p7.setId(Cayenne.intPKForObject(e14) * 100);
				p7.setString("p7_" + e14.getName());
				e14.setP7(p7);
			}
		}
	}

}
