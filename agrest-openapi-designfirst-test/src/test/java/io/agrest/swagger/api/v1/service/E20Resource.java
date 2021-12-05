package io.agrest.swagger.api.v1.service;

import io.agrest.cayenne.cayenne.main.E20;

import io.agrest.AgRequest;
import io.agrest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.agrest.Ag;
import io.agrest.SimpleResponse;

@Path("/")
public class E20Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e20")
    @Consumes({ "application/json" })
    public DataResponse<E20> createE20(String E20, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addExcludes(excludes)
                .build();

        return Ag.create(E20.class, config)
                 .request(agRequest)
                 .syncAndSelect(E20);
    }

    @DELETE
    @Path("/v1/e20/{name}")
    public SimpleResponse deleteE20ByName(@PathParam("name") String name) {

        return Ag.delete(E20.class, config)
                 .id(name)
                 .sync();
    }

    @GET
    @Path("/v1/e20/{name}")
    @Produces({ "application/json" })
        public DataResponse<E20> getOneE20ByName(@PathParam("name") String name, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addExcludes(excludes)
                .build();

        return Ag.select(E20.class, config)
                 .byId(name)
                 .request(agRequest)
                 .get();
    }

    @PUT
    @Path("/v1/e20/{name}")
    @Consumes({ "application/json" })
    public DataResponse<E20> updateE20ByName(@PathParam("name") String name, String E20) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E20.class, config)
                 .id(name)
                 .request(agRequest)
                 .syncAndSelect(E20);
    }

}
