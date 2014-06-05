package com.nhl.link.rest.unit.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.runtime.ILinkRestService;
import com.nhl.link.rest.runtime.LinkRestRuntime;
import com.nhl.link.rest.unit.cayenne.E6;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

@Path("charpk")
public class CharPkResources {

    @Context
    private Configuration config;

    private ILinkRestService getLinkRestService() {
        return LinkRestRuntime.service(ILinkRestService.class, config);
    }

    @GET
    @Path("{id}")
    public DataResponse<E6> getOne(@PathParam("id") String id) {
        return getLinkRestService().selectById(E6.class, id);
    }
}
