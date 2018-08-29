package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.protocol.Sort;
import com.nhl.link.rest.protocol.Start;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E3Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> create(String e3, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("exclude") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.create(E3.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e3);
    }

    @DELETE
    @Path("/v1/e3/{id}/e2")
    public SimpleResponse deleteE2ViaE3(@PathParam("id") Integer id) {

        return LinkRest.service(config)
                    .unrelate(E3.class, id, "e2");
    }

    @DELETE
    @Path("/v1/e3/{id}/e2/{tid}")
    public SimpleResponse deleteE2ViaE3_1(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return LinkRest.service(config)
                    .unrelate(E3.class, id, "e2", tid);
    }

    @GET
    @Path("/v1/e3")
    @Produces({ "application/json" })
    public DataResponse<E3> getAll(@QueryParam("sort") Sort sort, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("exclude") List<com.nhl.link.rest.protocol.Exclude> excludes, @QueryParam("limit") Limit limit, @QueryParam("start") Start start, @QueryParam("mapBy") MapBy mapBy, @QueryParam("cayenneExp") CayenneExp cayenneExp) {

        LrRequest lrRequest = LrRequest.builder()
                .sort(sort)
                .includes(includes)
                .excludes(excludes)
                .limit(limit)
                .start(start)
                .mapBy(mapBy)
                .cayenneExp(cayenneExp)
                .build();

        return LinkRest.select(E3.class, config)
                    .request(lrRequest)
                    
                    .get();
    }

    @GET
    @Path("/v1/e3/{id}")
    @Produces({ "application/json" })
        public DataResponse<E3> getOne(@PathParam("id") Integer id, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .build();

        return LinkRest.select(E3.class, config)
                    .byId(id)
                    .request(lrRequest)
                    
                    .get();
    }

    @GET
    @Path("/v1/e3/{id}/e2")
    @Produces({ "application/json" })
        public DataResponse<E2> getOneByOne(@PathParam("id") Integer id, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .build();

        return LinkRest.select(E2.class, config)
                    .parent(E3.class, id, "e2")
                    .request(lrRequest)
                    
                    .get();
    }

    @PUT
    @Path("/v1/e3/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E3> update(@PathParam("id") Integer id, String e3) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E3.class, config)
                    .id(id)
                    .request(lrRequest)
                    
                    .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateAll(String e3, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("exclude") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.idempotentCreateOrUpdate(E3.class, config)
                    .request(lrRequest)
                    
                    .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3/{id}/e2/{tid}")
    @Consumes({ "application/json" })
    public DataResponse<E2> updateE2ViaE3(@PathParam("id") Integer id, @PathParam("tid") Integer tid, String e2) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E2.class, config)
                    .id(tid)
                    .parent(E3.class, id, E3.E2)
                    .request(lrRequest)
                    
                    .syncAndSelect(e2);
    }

}
