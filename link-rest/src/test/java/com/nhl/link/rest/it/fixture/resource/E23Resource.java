package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.it.fixture.cayenne.E23;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

@Path("e23")
public class E23Resource {

    @Context
    private Configuration config;

    @GET
    @Path("{id}")
    public DataResponse<E23> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
        return LinkRest.select(E23.class, config).byId(id).uri(uriInfo).selectOne();
    }
}
