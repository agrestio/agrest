package io.agrest.swagger.api.v1.service;

import io.agrest.cayenne.cayenne.main.E21;

import io.agrest.AgRequest;
import io.agrest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import io.agrest.Ag;
import io.agrest.SimpleResponse;

@Path("/")
public class E21Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> create(String e21, @QueryParam("exclude") List<String> excludes) {

        AgRequest agRequest = Ag.request(config)
                .addExcludes(excludes)
                .build();

        return Ag.create(E21.class, config)
                 .request(agRequest)
                 .syncAndSelect(e21);
    }

    @DELETE
    @Path("/v1/e21")
    public SimpleResponse deleteByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age) {
        Map<String, Object> id = new HashMap<>();
        id.put("name", name);
        id.put("age", age);

        return Ag.delete(E21.class, config)
                 .id(id)
                 .delete();
    }

    @GET
    @Path("/v1/e21")
    @Produces({ "application/json" })
    public DataResponse<E21> getOneByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, @QueryParam("exclude") List<String> excludes) {

        Map<String, Object> id = new HashMap<>();
        id.put("name", name);
        id.put("age", age);

        AgRequest agRequest = Ag.request(config)
                .addExcludes(excludes)
                .build();

        return Ag.select(E21.class, config)
                 .byId(id)
                 .request(agRequest)
                 .getOne();
    }

    @PUT
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> updateByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, String e21) {

        Map<String, Object> id = new HashMap<>();
                id.put("name", name);
                id.put("age", age);


        AgRequest agRequest = Ag.request(config)
                .build();

        return Ag.idempotentCreateOrUpdate(E21.class, config)
                 .id(id)
                 .request(agRequest)
                 .syncAndSelect(e21);
    }

}
