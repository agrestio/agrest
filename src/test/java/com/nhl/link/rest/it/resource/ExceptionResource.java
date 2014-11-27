package com.nhl.link.rest.it.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;

@Path("nodata")
public class ExceptionResource {

	@GET
	public Response get() {
		throw new LinkRestException(Status.NOT_FOUND, "request failed");
	}

	@GET
	@Path("th")
	public Response getTh() {
		try {
			throw new Throwable("Dummy");
		} catch (Throwable th) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "request failed with th", th);
		}
	}
}
