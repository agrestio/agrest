package io.agrest.swagger.api.v1.service;

import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;

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
    public DataResponse<E3> create(String e3, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.create(E3.class, config)
                 .request(agRequest)
                 .syncAndSelect(e3);
    }

    @DELETE
    @Path("/v1/e3/{id}/e2")
    public SimpleResponse deleteE2ViaE3(@PathParam("id") Integer id) {

        return Ag.service(config)
                 .unrelate(E3.class, id, "e2");
    }

    @DELETE
    @Path("/v1/e3/{id}/e2/{tid}")
    public SimpleResponse deleteE2ViaE3_1(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return Ag.service(config)
                 .unrelate(E3.class, id, "e2", tid);
    }

    @GET
    @Path("/v1/e3")
    @Produces({ "application/json" })
    public DataResponse<E3> getAll(@QueryParam("sort") String sort, @QueryParam("dir") String dir, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes, @QueryParam("limit") Integer limit, @QueryParam("start") Integer start, @QueryParam("mapBy") String mapBy, @QueryParam("cayenneExp") String cayenneExp) {

        AgRequest agRequest = Ag.request(config)
                .addOrdering(sort, dir)
                .addIncludes(includes)
                .addExcludes(excludes)
                .limit(limit)
                .start(start)
                .mapBy(mapBy)
                .cayenneExp(cayenneExp)
                .build();

        return Ag.select(E3.class, config)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e3/{id}")
    @Produces({ "application/json" })
        public DataResponse<E3> getOne(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

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
        public DataResponse<E2> getOneByOne(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E2.class, config)
                 .parent(E3.class, id, "e2")
                 .request(agRequest)
                 .get();
    }

    @PUT
    @Path("/v1/e3/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E3> update(@PathParam("id") Integer id, String e3) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E3.class, config)
                 .id(id)
                 .request(agRequest)
                 .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateAll(String e3, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.idempotentCreateOrUpdate(E3.class, config)
                 .request(agRequest)
                 .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3/{id}/e2/{tid}")
    @Consumes({ "application/json" })
    public DataResponse<E2> updateE2ViaE3(@PathParam("id") Integer id, @PathParam("tid") Integer tid, String e2) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E2.class, config)
                 .id(tid)
                 .parent(E3.class, id, E3.E2)
                 .request(agRequest)
                 .syncAndSelect(e2);
    }

}
