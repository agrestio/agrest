package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E20;

import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E20Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e20")
    @Consumes({ "application/json" })
    public DataResponse<E20> create(String e20, @QueryParam("exclude") List<String> exclude) {

        return LinkRest.create(E20.class, config)
                    .readConstraint(Constraint.excludeAll(E20.class).includeId().attributes("description", "age", "name")
                        .path("e21",Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                        )

                    )
                    
                    .exclude(exclude)

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
        public DataResponse<E20> getOneByName(@PathParam("name") String name, @QueryParam("exclude") List<String> exclude) {

        return LinkRest.select(E20.class, config)
                    .constraint(Constraint.excludeAll(E20.class).includeId().attributes("description", "age", "name")
                        .path("e21",Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                        )

                    )
                    .byId(name)
                    
                    .exclude(exclude)

                    .get();
    }

    @PUT
    @Path("/v1/e20/{name}")
    @Consumes({ "application/json" })
    public DataResponse<E20> updateByName(@PathParam("name") String name, String e20) {

        return LinkRest.idempotentCreateOrUpdate(E20.class, config)
                    .readConstraint(Constraint.excludeAll(E20.class).includeId().attributes("description", "age", "name")
                        .path("e21",Constraint.excludeAll(E21.class).includeId().attributes("name", "description", "age")

                        )

                    )
                    .id(name)
                    

                    .syncAndSelect(e20);
    }

}
