package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.protocol.Limit;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E1Resource {

    @Context
    private Configuration config;

    @DELETE
    @Path("/v1/e1/{id}")
    public SimpleResponse delete(@PathParam("id") Integer id) {

        return LinkRest.delete(E1.class, config)
                    .id(id)
                    .delete();
    }

    @GET
    @Path("/v1/e1")
    @Produces({ "application/json" })
    public DataResponse<E1> getAll(@QueryParam("limit") Limit limit) {

        LrRequest lrRequest = LrRequest.builder()
                .limit(limit)
                .build();

        return LinkRest.select(E1.class, config)
                    .constraint(Constraint.excludeAll(E1.class).includeId().attributes("name", "description", "age")

                    )
                    .request(lrRequest)
                    .get();
    }

    @GET
    @Path("/v1/e1/{id}")
    @Produces({ "application/json" })
        public DataResponse<E1> getOne(@PathParam("id") Integer id) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.select(E1.class, config)
                    .constraint(Constraint.excludeAll(E1.class).includeId().attributes("name", "description", "age")

                    )
                    .byId(id)
                    .request(lrRequest)
                    .get();
    }

}
