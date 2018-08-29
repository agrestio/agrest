package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.E21;

import com.nhl.link.rest.LrRequest;
import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
    import com.nhl.link.rest.runtime.processor.update.UpdateContext;
    import com.nhl.link.rest.runtime.processor.select.SelectContext;
    import com.nhl.link.rest.UpdateStage;
    import com.nhl.link.rest.SelectStage;
    import com.nhl.link.rest.swagger.api.v1.service.stage.E21ResourceStage;

@Path("/")
public class E21Resource extends E21ResourceStage {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> create(String e21, @QueryParam("exclude") List<com.nhl.link.rest.protocol.Exclude> excludes) {

        LrRequest lrRequest = LrRequest.builder()
                .excludes(excludes)
                .build();

        return LinkRest.create(E21.class, config)
                    .request(lrRequest)
                    .syncAndSelect(e21);
    }

    @DELETE
    @Path("/v1/e21")
    public SimpleResponse deleteByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age) {
        Map<String, Object> id = new HashMap<>();
        id.put("name", name);
        id.put("age", age);

        return LinkRest.delete(E21.class, config)
                    .id(id)
                    .delete();
    }

    @GET
    @Path("/v1/e21")
    @Produces({ "application/json" })
    public DataResponse<E21> getOneByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, @QueryParam("abc") Integer abc, @QueryParam("exclude") List<com.nhl.link.rest.protocol.Exclude> excludes, @QueryParam("xyz") List<String> xyzs) {

        Map<String, Object> id = new HashMap<>();
        id.put("name", name);
        id.put("age", age);

        LrRequest lrRequest = LrRequest.builder()
                .excludes(excludes)
                .build();

        return LinkRest.select(E21.class, config)
                    .byId(id)
                    .request(lrRequest)
                    .stage(SelectStage.PARSE_REQUEST, (SelectContext<E21> c) -> getOneByCompoundIdImpl(c, xyzs, abc))

                    .getOne();
    }

    @PUT
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> updateByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, String e21, @QueryParam("xyz") List<String> xyzs) {

        Map<String, Object> id = new HashMap<>();
                id.put("name", name);
                id.put("age", age);


        LrRequest lrRequest = LrRequest.builder()
                .build();

        return LinkRest.idempotentCreateOrUpdate(E21.class, config)
                    .id(id)
                    .request(lrRequest)
                    .stage(UpdateStage.PARSE_REQUEST, (UpdateContext<E21> c) -> updateByCompoundIdImpl(c, xyzs))

                    .syncAndSelect(e21);
    }

}
