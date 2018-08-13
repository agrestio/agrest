package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.it.fixture.cayenne.E2;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E2Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e2")
    @Consumes({ "application/json" })
    public DataResponse<E2> create(String e2, @QueryParam("includes") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("excludes") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.create(E2.class, config)
                    .readConstraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address", "e3s")

                    )
                    .request(lrRequest)
                    .syncAndSelect(e2);
    }

    @GET
    @Path("/v1/e2")
    @Produces({ "application/json" })
    public DataResponse<E2> getAll(@QueryParam("includes") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("excludes") List<com.nhl.link.rest.protocol.Exclude> excludes, @QueryParam("cayenneExp") CayenneExp cayenneExp) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .cayenneExp(cayenneExp)
                .build();

        return LinkRest.select(E2.class, config)
                    .constraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address", "e3s")

                    )
                    .request(lrRequest)
                    .get();
    }

    @GET
    @Path("/v1/e2/{id}")
    @Produces({ "application/json" })
        public DataResponse<E2> getOne(@PathParam("id") Integer id, @QueryParam("includes") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("excludes") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.select(E2.class, config)
                    .constraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address", "e3s")

                    )
                    .byId(id)
                    .request(lrRequest)
                    .get();
    }

    @PUT
    @Path("/v1/e2/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E2> update(@PathParam("id") Integer id, String e2, @QueryParam("includes") List<com.nhl.link.rest.protocol.Include> includes, @QueryParam("excludes") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.idempotentCreateOrUpdate(E2.class, config)
                    .readConstraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address", "e3s")

                    )
                    .id(id)
                    .request(lrRequest)
                    .syncAndSelect(e2);
    }

}
