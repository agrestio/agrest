package com.nhl.link.rest.swagger.api.v1.service;

import com.nhl.link.rest.it.fixture.cayenne.*;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;

import com.nhl.link.rest.DataResponse;

import java.util.*;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;

@Path("/")
public class E3Resource {

    @Context
    private Configuration config;

    @POST
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> create(String e3, @QueryParam("include") List<String> include, @QueryParam("exclude") List<String> exclude) {

        return LinkRest.create(E3.class, config)
                    .readConstraint(Constraint.excludeAll(E3.class).includeId().attributes("name", "phoneNumber")
                        .path("e2",Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                        )
                        .path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")

                        )

                    )
                    
                    .include(include)
                    .exclude(exclude)

                    .syncAndSelect(e3);
    }

    @DELETE
    @Path("/v1/e3/{id}/e2")
    public SimpleResponse deleteE2ViaE3(@PathParam("id") Integer id) {

        return LinkRest.service(config)
                    .unrelate(E3.class, id, "e2");
    }

    @DELETE
    @Path("/v1/e3/{id}/e2/{tid}")
    public SimpleResponse deleteE2ViaE3_1(@PathParam("id") Integer id, @PathParam("tid") Integer tid) {

        return LinkRest.service(config)
                    .unrelate(E3.class, id, "e2", tid);
    }

    @GET
    @Path("/v1/e3")
    @Produces({ "application/json" })
    public DataResponse<E3> getAll(@QueryParam("sort") String sort, @QueryParam("include") List<String> include, @QueryParam("exclude") List<String> exclude, @QueryParam("limit") Integer limit, @QueryParam("start") Integer start, @QueryParam("mapBy") String mapBy, @QueryParam("cayenneExp") String cayenneExp) {
        return LinkRest.select(E3.class, config)
                    .constraint(Constraint.excludeAll(E3.class).includeId().attributes("name", "phoneNumber")
                        .path("e2",Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                        )
                        .path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")

                        )

                    )
                    
                    .sort(sort)
                    .include(include)
                    .exclude(exclude)
                    .limit(limit)
                    .start(start)
                    .mapBy(mapBy)
                    .cayenneExp(cayenneExp)

                    .get();
    }

    @GET
    @Path("/v1/e3/{id}")
    @Produces({ "application/json" })
        public DataResponse<E3> getOne(@PathParam("id") Integer id, @QueryParam("include") List<String> include) {

        return LinkRest.select(E3.class, config)
                    .constraint(Constraint.excludeAll(E3.class).includeId().attributes("name", "phoneNumber")
                        .path("e2",Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                        )
                        .path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")

                        )

                    )
                    .byId(id)
                    
                    .include(include)

                    .get();
    }

    @GET
    @Path("/v1/e3/{id}/e2")
    @Produces({ "application/json" })
        public DataResponse<E2> getOneByOne(@PathParam("id") Integer id, @QueryParam("include") List<String> include) {

        return LinkRest.select(E2.class, config)
                    .constraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                    )
                    .parent(E3.class, id, "e2")
                    
                    .include(include)

                    .get();
    }

    @PUT
    @Path("/v1/e3/{id}")
    @Consumes({ "application/json" })
    public DataResponse<E3> update(@PathParam("id") Integer id, String e3) {

        return LinkRest.idempotentCreateOrUpdate(E3.class, config)
                    .readConstraint(Constraint.excludeAll(E3.class).includeId().attributes("name", "phoneNumber")
                        .path("e2",Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                        )
                        .path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")

                        )

                    )
                    .id(id)
                    

                    .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3")
    @Consumes({ "application/json" })
    public DataResponse<E3> updateAll(String e3, @QueryParam("include") List<String> include, @QueryParam("exclude") List<String> exclude) {

        return LinkRest.idempotentCreateOrUpdate(E3.class, config)
                    .readConstraint(Constraint.excludeAll(E3.class).includeId().attributes("name", "phoneNumber")
                        .path("e2",Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                        )
                        .path("e5",Constraint.excludeAll(E5.class).includeId().attributes("name", "date")

                        )

                    )
                    
                    .include(include)
                    .exclude(exclude)

                    .syncAndSelect(e3);
    }

    @PUT
    @Path("/v1/e3/{id}/e2/{tid}")
    @Consumes({ "application/json" })
    public DataResponse<E2> updateE2ViaE3(@PathParam("id") Integer id, @PathParam("tid") Integer tid, String e2) {

        return LinkRest.idempotentCreateOrUpdate(E2.class, config)
                    .readConstraint(Constraint.excludeAll(E2.class).includeId().attributes("name", "address")

                    )
                    .id(tid)
                    .parent(E3.class, id, E3.E2)
                    

                    .syncAndSelect(e2);
    }

}
