package com.nhl.link.rest.unit.resource;

import static com.nhl.link.rest.EntityConstraintsBuilder.excludeAll;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
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

		return getService().forSelect(E4.class).constraints(excludeAll().attributes(E4.C_INT)).select();
	}

}
