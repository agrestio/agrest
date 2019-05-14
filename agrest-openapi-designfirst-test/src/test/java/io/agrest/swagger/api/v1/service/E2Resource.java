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
public class E2Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e2")
    @Consumes({ "application/json" })
    public DataResponse<E2> create(String e2, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.create(E2.class, config)
                 .request(agRequest)
                 .syncAndSelect(e2);
    }

    @POST
    @Path("/v1/e2/{id}/e3s")
    @Consumes({ "application/json" })
    public DataResponse<E3> createE3sViaE2(@PathParam("id") Integer id, String e3) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.createOrUpdate(E3.class, config)
                 .parent(E2.class, id, "e3s")
                 .request(agRequest)
                 .syncAndSelect(e3);
    }

    @DELETE
    @Path("/v1/e2/{id}/e3s/{tid}")
    public SimpleResponse deleteE3ViaE2(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return Ag.service(config)
                 .unrelate(E2.class, id, "e3s", tid);
    }

    @DELETE
    @Path("/v1/e2/{id}/e3s")
    public SimpleResponse deleteE3sViaE2(@PathParam("id") Integer id) {

        return Ag.service(config)
                 .unrelate(E2.class, id, "e3s");
    }

    @GET
    @Path("/v1/e2")
    @Produces({ "application/json" })
    public DataResponse<E2> getAll(@QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes, @QueryParam("cayenneExp") String cayenneExp) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .cayenneExp(cayenneExp)
                .build();

        return Ag.select(E2.class, config)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e2/{id}/e3s/{tid}")
    @Produces({ "application/json" })
        public DataResponse<E3> getE3viaE2(@PathParam("id") Integer id, @PathParam("tid") Integer tid, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E3.class, config)
                 .byId(tid)
                 .parent(E2.class, id, "e3s")
                 .request(agRequest)
                 .getOne();
    }

    @GET
    @Path("/v1/e2/{id}")
    @Produces({ "application/json" })
        public DataResponse<E2> getOne(@PathParam("id") Integer id, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.select(E2.class, config)
                 .byId(id)
                 .request(agRequest)
                 .get();
    }

    @GET
    @Path("/v1/e2/{id}/e3s")
    @Produces({ "application/json" })
        public DataResponse<E3> getOneToMany(@PathParam("id") Integer id, @QueryParam("include") List<String> includes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .build();

        return Ag.select(E3.class, config)
                 .parent(E2.class, id, "e3s")
                 .request(agRequest)
                 .get();
    }

    @PUT
    @Path("/v1/e2/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E2> update(@PathParam("id") Integer id, String e2, @QueryParam("include") List<String> includes, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addIncludes(includes)
                .addExcludes(excludes)
                .build();

        return Ag.idempotentCreateOrUpdate(E2.class, config)
                 .id(id)
                 .request(agRequest)
                 .syncAndSelect(e2);
    }

    @PUT
    @Path("/v1/e2/{id}/e3s")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateE3sViaE2(@PathParam("id") Integer id, String e3) {

        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E3.class, config)
                 .parent(E2.class, id, "e3s")
                 .request(agRequest)
                 .syncAndSelect(e3);
    }

}
