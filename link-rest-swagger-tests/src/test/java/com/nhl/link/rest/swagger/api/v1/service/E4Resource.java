package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E4;

import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
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

        return LinkRest.create(E4.class, config)
                    .readConstraint(Constraint.excludeAll(E4.class).includeId().attributes("cDecimal", "cDate", "cTimestamp", "cTime", "cVarchar", "cBoolean", "cInt")

                    )
                    

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
    public DataResponse<E4> getAll(@QueryParam("limit") Integer limit, @QueryParam("sort") String sort, @QueryParam("include") List<String> include, @QueryParam("mapBy") String mapBy) {
        return LinkRest.select(E4.class, config)
                    .constraint(Constraint.excludeAll(E4.class).includeId().attributes("cDecimal", "cDate", "cTimestamp", "cTime", "cVarchar", "cBoolean", "cInt")

                    )
                    
                    .limit(limit)
                    .sort(sort)
                    .include(include)
                    .mapBy(mapBy)

                    .get();
    }

    @GET
    @Path("/v1/e4/{id}")
    @Produces({ "application/json" })
        public DataResponse<E4> getOne(@PathParam("id") Integer id, @QueryParam("include") List<String> include) {

        return LinkRest.select(E4.class, config)
                    .constraint(Constraint.excludeAll(E4.class).includeId().attributes("cDecimal", "cDate", "cTimestamp", "cTime", "cVarchar", "cBoolean", "cInt")

                    )
                    .byId(id)
                    
                    .include(include)

                    .get();
    }

    @PUT
    @Path("/v1/e4/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E4> update(@PathParam("id") Integer id, String e4) {

        return LinkRest.idempotentCreateOrUpdate(E4.class, config)
                    .readConstraint(Constraint.excludeAll(E4.class).includeId().attributes("cDecimal", "cDate", "cTimestamp", "cTime", "cVarchar", "cBoolean", "cInt")

                    )
                    .id(id)
                    

                    .syncAndSelect(e4);
    }

}
