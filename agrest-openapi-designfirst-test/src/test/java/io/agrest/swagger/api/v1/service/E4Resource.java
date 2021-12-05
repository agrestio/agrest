package io.agrest.swagger.api.v1.service;

import io.agrest.cayenne.cayenne.main.E4;

import io.agrest.AgRequest;
import io.agrest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.agrest.Ag;
import io.agrest.SimpleResponse;

@Path("/")
public class E4Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e4")
    @Consumes({ "application/json" })
    public DataResponse<E4> createE4(String E4) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.create(E4.class, config)
                 .request(agRequest)
                 .syncAndSelect(E4);
    }

    @DELETE
    @Path("/v1/e4/{id}")
    public SimpleResponse deleteE4(@PathParam("id") Integer id) {

        return Ag.delete(E4.class, config)
                 .id(id)
                 .sync();
    }

    @GET
    @Path("/v1/e4")
    @Produces({ "application/json" })
    public DataResponse<E4> getAllE4(@QueryParam("limit") Integer limit, @QueryParam("sort") String sort, @QueryParam("include") List<String> includes, @QueryParam("mapBy") String mapBy) {

        AgRequest agRequest = Ag.request(config)
                .limit(limit)
                .addOrdering(sort)
                .addIncludes(includes)
                .mapBy(mapBy)
                .build();

        return Ag.select(E4.class, config)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e4/{id}")
    @Produces({ "application/json" })
        public DataResponse<E4> getOneE4(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E4.class, config)
                 .byId(id)
                 .request(agRequest)
                 .get();
    }

    @PUT
    @Path("/v1/e4/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E4> updateE4(@PathParam("id") Integer id, String E4) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E4.class, config)
                 .id(id)
                 .request(agRequest)
                 .syncAndSelect(E4);
    }

}
