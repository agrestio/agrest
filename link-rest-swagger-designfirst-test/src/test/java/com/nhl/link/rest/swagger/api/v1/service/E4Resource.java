package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.protocol.Limit;
import com.nhl.link.rest.protocol.MapBy;
import com.nhl.link.rest.protocol.Sort;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E4Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e4")
    @Consumes({ "application/json" })
    public DataResponse<E4> create(String e4) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.create(E4.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e4);
    }

    @DELETE
    @Path("/v1/e4/{id}")
    public SimpleResponse delete(@PathParam("id") Integer id) {

        return LinkRest.delete(E4.class, config)
                    .id(id)
                    .delete();
    }

    @GET
    @Path("/v1/e4")
    @Produces({ "application/json" })
    public DataResponse<E4> getAll(@QueryParam("limit") Limit limit, @QueryParam("sort") Sort sort, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("mapBy") MapBy mapBy) {

        LrRequest lrRequest = LrRequest.builder()
                .limit(limit)
                .sort(sort)
                .includes(includes)
                .mapBy(mapBy)
                .build();

        return LinkRest.select(E4.class, config)
                    .request(lrRequest)
                    
                    .get();
    }

    @GET
    @Path("/v1/e4/{id}")
    @Produces({ "application/json" })
        public DataResponse<E4> getOne(@PathParam("id") Integer id, @QueryParam("include") List<com.nhl.link.rest.protocol.Include> includes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .build();

        return LinkRest.select(E4.class, config)
                    .byId(id)
                    .request(lrRequest)
                    
                    .get();
    }

    @PUT
    @Path("/v1/e4/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E4> update(@PathParam("id") Integer id, String e4) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E4.class, config)
                    .id(id)
                    .request(lrRequest)
                    
                    .syncAndSelect(e4);
    }

}
