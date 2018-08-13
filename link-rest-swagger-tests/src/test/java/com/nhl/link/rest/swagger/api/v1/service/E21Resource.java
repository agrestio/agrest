package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E21;

import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E21Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> create(String e21, @QueryParam("exclude") List<String> exclude) {

        return LinkRest.create(E21.class, config)
                    .readConstraint(Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                    )
                    
                    .exclude(exclude)

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
    public DataResponse<E21> getOneByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, @QueryParam("exclude") List<String> exclude) {

        Map<String, Object> id = new HashMap<>();
        id.put("name", name);
        id.put("age", age);

        return LinkRest.select(E21.class, config)
                    .constraint(Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                    )
                    .byId(id)
                    
                    .exclude(exclude)

                    .getOne();
    }

    @PUT
    @Path("/v1/e21")
    @Consumes({ "application/json" })
    public DataResponse<E21> updateByCompoundId(@QueryParam("name") String name, @QueryParam("age") Integer age, String e21) {

        Map<String, Object> id = new HashMap<>();
                id.put("name", name);
                id.put("age", age);

        return LinkRest.idempotentCreateOrUpdate(E21.class, config)
                    .readConstraint(Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                    )
                    .id(id)
                    

                    .syncAndSelect(e21);
    }

}
