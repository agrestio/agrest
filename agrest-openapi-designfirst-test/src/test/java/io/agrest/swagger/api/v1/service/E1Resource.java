package io.agrest.swagger.api.v1.service;

import io.agrest.Ag;
import io.agrest.AgRequest;
import io.agrest.DataResponse;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.cayenne.E1;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;

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
    public DataResponse<E1> getAll(@QueryParam("limit") Integer limit) {

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
