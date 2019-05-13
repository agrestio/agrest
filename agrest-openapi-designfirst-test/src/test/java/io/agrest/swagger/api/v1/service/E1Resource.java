package io.agrest.swagger.api.v1.service;

import io.agrest.it.fixture.cayenne.E1;
import io.agrest.protocol.Limit;

import io.agrest.AgRequest;
import io.agrest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.agrest.Ag;
import io.agrest.SimpleResponse;

@Path("/")
public class E1Resource {

    @Context
    private Configuration config;

    @DELETE
    @Path("/v1/e1/{id}")
    public SimpleResponse delete(@PathParam("id") Integer id) {

        return Ag.delete(E1.class, config)
                 .id(id)
                 .delete();
    }

    @GET
    @Path("/v1/e1")
    @Produces({ "application/json" })
    public DataResponse<E1> getAll(@QueryParam("limit") Limit limit) {

        AgRequest agRequest = Ag.request(config)
                .limit(limit)
                .build();

        return Ag.select(E1.class, config)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e1/{id}")
    @Produces({ "application/json" })
        public DataResponse<E1> getOne(@PathParam("id") Integer id) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.select(E1.class, config)
                 .byId(id)
                 .request(agRequest)
                 .get();
    }

}
