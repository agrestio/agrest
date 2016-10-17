package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E24;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

@Path("e24")
public class E24Resource {

    @Context
    private Configuration config;

    @DELETE
    @Path("{id}")
    public SimpleResponse delete(@PathParam("id") int id) {
        return LinkRest.delete(E24.class, config).id(id).delete();
    }
}
