package io.agrest.swagger.api.v1.service;

import com.nhl.link.rest.protocol.CayenneExp;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;

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
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.protocol.CayenneExp;
import io.agrest.protocol.Exclude;
import io.agrest.protocol.Include;

@Path("/")
public class E2Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e2")
    @Consumes({ "application/json" })
    public DataResponse<E2> create(String e2, @QueryParam("include") List<Include> includes, @QueryParam("exclude") List<Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.create(E2.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e2);
    }

    @POST
    @Path("/v1/e2/{id}/e3s")
    @Consumes({ "application/json" })
    public DataResponse<E3> createE3sViaE2(@PathParam("id") Integer id, String e3) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.createOrUpdate(E3.class, config)
                    .parent(E2.class, id, "e3s")
                    .request(lrRequest)
                    .syncAndSelect(e3);
    }

    @DELETE
    @Path("/v1/e2/{id}/e3s/{tid}")
    public SimpleResponse deleteE3ViaE2(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return LinkRest.service(config)
                    .unrelate(E2.class, id, "e3s", tid);
    }

    @DELETE
    @Path("/v1/e2/{id}/e3s")
    public SimpleResponse deleteE3sViaE2(@PathParam("id") Integer id) {

        return LinkRest.service(config)
                    .unrelate(E2.class, id, "e3s");
    }

    @GET
    @Path("/v1/e2")
    @Produces({ "application/json" })
    public DataResponse<E2> getAll(@QueryParam("include") List<Include> includes, @QueryParam("exclude") List<Exclude> excludes, @QueryParam("cayenneExp") CayenneExp cayenneExp) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .cayenneExp(cayenneExp)
                .build();

        return LinkRest.select(E2.class, config)
                    .request(lrRequest)
                    .get();
    }

    @GET
    @Path("/v1/e2/{id}/e3s/{tid}")
    @Produces({ "application/json" })
        public DataResponse<E3> getE3viaE2(@PathParam("id") Integer id, @PathParam("tid") Integer tid, @QueryParam("include") List<Include> includes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .build();

        return LinkRest.select(E3.class, config)
                    .byId(tid)
                    .parent(E2.class, id, "e3s")
                    .request(lrRequest)
                    .getOne();
    }

    @GET
    @Path("/v1/e2/{id}")
    @Produces({ "application/json" })
        public DataResponse<E2> getOne(@PathParam("id") Integer id, @QueryParam("include") List<Include> includes, @QueryParam("exclude") List<Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.select(E2.class, config)
                    .byId(id)
                    .request(lrRequest)
                    .get();
    }

    @GET
    @Path("/v1/e2/{id}/e3s")
    @Produces({ "application/json" })
        public DataResponse<E3> getOneToMany(@PathParam("id") Integer id, @QueryParam("include") List<Include> includes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .build();

        return LinkRest.select(E3.class, config)
                    .parent(E2.class, id, "e3s")
                    .request(lrRequest)
                    .get();
    }

    @PUT
    @Path("/v1/e2/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E2> update(@PathParam("id") Integer id, String e2, @QueryParam("include") List<Include> includes, @QueryParam("exclude") List<Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .includes(includes)
                .excludes(excludes)
                .build();

        return LinkRest.idempotentCreateOrUpdate(E2.class, config)
                    .id(id)
                    .request(lrRequest)
                    .syncAndSelect(e2);
    }

    @PUT
    @Path("/v1/e2/{id}/e3s")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateE3sViaE2(@PathParam("id") Integer id, String e3) {

        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E3.class, config)
                    .parent(E2.class, id, "e3s")
                    .request(lrRequest)
                    .syncAndSelect(e3);
    }

}
