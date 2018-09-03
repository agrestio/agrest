package io.agrest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.E20;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import io.agrest.DataResponse;
import io.agrest.LinkRest;
import io.agrest.LrRequest;
import io.agrest.SimpleResponse;
import io.agrest.it.fixture.cayenne.E20;
import io.agrest.protocol.Exclude;

@Path("/")
public class E20Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e20")
    @Consumes({ "application/json" })
    public DataResponse<E20> create(String e20, @QueryParam("exclude") List<Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .excludes(excludes)
                .build();

        return LinkRest.create(E20.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e20);
    }

    @DELETE
    @Path("/v1/e20/{name}")
    public SimpleResponse deleteByName(@PathParam("name") String name) {

        return LinkRest.delete(E20.class, config)
                    .id(name)
                    .delete();
    }

    @GET
    @Path("/v1/e20/{name}")
    @Produces({ "application/json" })
        public DataResponse<E20> getOneByName(@PathParam("name") String name, @QueryParam("exclude") List<Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .excludes(excludes)
                .build();

        return LinkRest.select(E20.class, config)
                    .byId(name)
                    .request(lrRequest)
                    .get();
    }

    @PUT
    @Path("/v1/e20/{name}")
    @Consumes({ "application/json" })
    public DataResponse<E20> updateByName(@PathParam("name") String name, String e20) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E20.class, config)
                    .id(name)
                    .request(lrRequest)
                    .syncAndSelect(e20);
    }

}
