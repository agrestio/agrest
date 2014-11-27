package com.nhl.link.rest.it.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.nhl.link.rest.SimpleResponse;

@Path("simple")
public class SimpleResponseResource {

	@GET
	public SimpleResponse get() {
		return new SimpleResponse(true, "Hi!");
	}

	@GET
	@Path("2")
	public SimpleResponse get2() {
		return new SimpleResponse(false, "Hi2!");
	}
}
