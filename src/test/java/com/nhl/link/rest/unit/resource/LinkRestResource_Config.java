package com.nhl.link.rest.unit.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.DataResponseConfig;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E4;

@Path("lrc")
public class LinkRestResource_Config {

	@Context
	private Configuration config;

	private ILinkRestService getLinkRestService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}

	@GET
	@Path("limit_attributes")
	public DataResponse<E4> getObjects_LimitAttributes(@Context UriInfo uriInfo) {

		DataResponseConfig config = getLinkRestService().newConfig(E4.class);
		config.getEntity().excludeAttributes().attributes(E4.C_INT);

		return getLinkRestService().forSelect(E4.class).withConfig(config).select();
	}

}
