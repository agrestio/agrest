package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.E5;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E5Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e5")
    @Consumes({ "application/json" })
    public DataResponse<E5> create(String e5) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.create(E5.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e5);
    }

    @GET
    @Path("/v1/e5")
    @Produces({ "application/json" })
    public DataResponse<E5> getAll() {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.select(E5.class, config)
                    .request(lrRequest)
                    .get();
    }

}
