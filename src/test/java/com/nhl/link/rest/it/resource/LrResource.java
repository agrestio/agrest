package com.nhl.link.rest.it.resource;

import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;

public abstract class LrResource {

	@Context
	private Configuration config;

	protected ILinkRestService getService() {
		return LinkRestRuntime.service(ILinkRestService.class, config);
	}
}
