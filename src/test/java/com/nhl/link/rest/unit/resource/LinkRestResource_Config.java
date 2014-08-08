package com.nhl.link.rest.unit.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.SelectBuilder;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E4;

@Path("lrc")
public class LinkRestResource_Config {

	@Context
	private Configuration config;

	private ILinkRestService getService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	@GET
	@Path("limit_attributes")
	public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {

		SelectBuilder<E4> sb = getService().forSelect(E4.class);

		sb.getConfig().getEntity().excludeAttributes().attributes(E4.C_INT);

		return sb.select();
	}

}
