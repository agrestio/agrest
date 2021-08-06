package io.agrest.swagger.api.v1.service;

import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.cayenne.main.E3;

import io.agrest.AgRequest;
import io.agrest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.agrest.Ag;
import io.agrest.SimpleResponse;

@Path("/")
public class E3Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> createE3(String E3, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.create(E3.class, config)
                 .request(agRequest)
                 .syncAndSelect(E3);
    }

    @DELETE
    @Path("/v1/e3/{id}/e2")
    public SimpleResponse deleteE2ViaE3(@PathParam("id") Integer id) {

        return Ag.service(config)
                 .unrelate(E3.class, id, "e2");
    }

    @DELETE
    @Path("/v1/e3/{id}/e2/{tid}")
    public SimpleResponse deleteE2ViaE3WithTid(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return Ag.service(config)
                 .unrelate(E3.class, id, "e2", tid);
    }

    @GET
    @Path("/v1/e3")
    @Produces({ "application/json" })
    public DataResponse<E3> getAllE3(@QueryParam("sort") String sort, @QueryParam("dir") String dir, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes, @QueryParam("limit") Integer limit, @QueryParam("start") Integer start, @QueryParam("mapBy") String mapBy, @QueryParam("exp") String exp) {

        AgRequest agRequest = Ag.request(config)
                .addOrdering(sort, dir)
                .addIncludes(includes)
                .addExcludes(excludes)
                .limit(limit)
                .start(start)
                .mapBy(mapBy)
                .exp(exp)
                .build();

        return Ag.select(E3.class, config)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e3/{id}")
    @Produces({ "application/json" })
        public DataResponse<E3> getOneE3(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E3.class, config)
                 .byId(id)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e3/{id}/e2")
    @Produces({ "application/json" })
        public DataResponse<E2> getOneE3ByOneE2(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E2.class, config)
                 .parent(E3.class, id, "e2")
                 .request(agRequest)
                 .get();
    }

    @PUT
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateAllE3(String E3, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.idempotentCreateOrUpdate(E3.class, config)
                 .request(agRequest)
                 .syncAndSelect(E3);
    }

    @PUT
    @Path("/v1/e3/{id}/e2/{tid}")
    @Consumes({ "application/json" })
    public DataResponse<E2> updateE2ViaE3(@PathParam("id") Integer id, @PathParam("tid") Integer tid, String E2) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E2.class, config)
                 .id(tid)
                 .parent(E3.class, id, "e2")
                 .request(agRequest)
                 .syncAndSelect(E2);
    }

    @PUT
    @Path("/v1/e3/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateE3(@PathParam("id") Integer id, String E3) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E3.class, config)
                 .id(id)
                 .request(agRequest)
                 .syncAndSelect(E3);
    }

}
