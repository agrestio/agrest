package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E25;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

/**
 * Generic Cayenne entity,
 */
@Path("e25")
public class E25Resource {

    @Context
    private Configuration config;

    @GET
    public DataResponse<E25> getAll(@Context UriInfo uriInfo) {
        return LinkRest.select(E25.class, config).uri(uriInfo).get();
    }
}
