package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.UpdateStage;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.it.fixture.listener.FetchCallbackListener;
import com.nhl.link.rest.it.fixture.listener.FetchPassThroughListener;
import com.nhl.link.rest.it.fixture.listener.FetchTakeOverListener;
import com.nhl.link.rest.it.fixture.listener.UpdateCallbackListener;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

@Path("e3")
public class E3Resource {

    @Context
    private Configuration config;

    @GET
    public DataResponse<E3> get(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E3.class).uri(uriInfo).get();
    }

    /**
     * @deprecated since 2.7 as listener API is deprecated.
     */
    @GET
    @Path("callbacklistener")
    public DataResponse<E3> getWithCallbackListeners(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E3.class).listener(new FetchCallbackListener()).uri(uriInfo).get();
    }

    /**
     * @deprecated since 2.7 as listener API is deprecated.
     */
    @GET
    @Path("passthroughlistener")
    public DataResponse<E3> getWithPassThroughListeners(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E3.class).listener(new FetchPassThroughListener()).uri(uriInfo).get();
    }

    /**
     * @deprecated since 2.7 as listener API is deprecated.
     */
    @GET
    @Path("takeoverlistener")
    public DataResponse<E3> getWithTakeOverListeners(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E3.class).listener(new FetchTakeOverListener()).uri(uriInfo).get();
    }

    @POST
    public DataResponse<E3> create(@Context UriInfo uriInfo, String requestBody) {
        return LinkRest.create(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
    }

    @PUT
    public DataResponse<E3> sync(@Context UriInfo uriInfo, String requestBody) {
        return LinkRest.idempotentFullSync(E3.class, config).uri(uriInfo).syncAndSelect(requestBody);
    }

    @PUT
    @Path("updatebinding")
    public SimpleResponse sync_EntityUpdateCollection(@Context UriInfo uriInfo,
                                                      Collection<EntityUpdate<E3>> entityUpdates) {
        return LinkRest.idempotentFullSync(E3.class, config).uri(uriInfo).sync(entityUpdates);
    }

    /**
     * @deprecated since 2.7 in favor of {@link #syncWithCallbackStage(UriInfo, String)}.
     */
    @PUT
    @Path("callbacklistener")
    public DataResponse<E3> syncWithCallbackListeners(@Context UriInfo uriInfo, String requestBody) {
        return LinkRest.idempotentFullSync(E3.class, config).listener(new UpdateCallbackListener()).uri(uriInfo)
                .syncAndSelect(requestBody);
    }

    @PUT
    @Path("callbackstage")
    public DataResponse<E3> syncWithCallbackStage(@Context UriInfo uriInfo, String requestBody) {
        return LinkRest.idempotentFullSync(E3.class, config)
                .stage(UpdateStage.APPLY_SERVER_PARAMS, c -> UpdateCallbackListener.BEFORE_UPDATE_CALLED = true)
                .uri(uriInfo)
                .syncAndSelect(requestBody);
    }

    @GET
    @Path("{id}")
    public DataResponse<E3> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
        return LinkRest.service(config).selectById(E3.class, id, uriInfo);
    }

    @PUT
    @Path("{id}")
    public DataResponse<E3> updateE3(@PathParam("id") int id, String requestBody) {
        return LinkRest.update(E3.class, config).id(id).syncAndSelect(requestBody);
    }

    @PUT
    @Path("updatebinding/{id}")
    public SimpleResponse updateE3_EntityUpdateSingle(@PathParam("id") int id, EntityUpdate<E3> update) {
        return LinkRest.createOrUpdate(E3.class, config).id(id).sync(update);
    }

    @POST
    @Path("constrained")
    public DataResponse<E3> insertReadConstrained(@Context UriInfo uriInfo, String requestBody) {
        Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
        return LinkRest.create(E3.class, config).uri(uriInfo).readConstraint(tc).syncAndSelect(requestBody);
    }

    @POST
    @Path("w/constrained")
    public DataResponse<E3> insertWriteConstrained(@Context UriInfo uriInfo, String requestBody) {
        Constraint<E3> tc = Constraint.idOnly(E3.class).attribute(E3.NAME);
        return LinkRest.create(E3.class, config).uri(uriInfo).writeConstraint(tc).syncAndSelect(requestBody);
    }

    @GET
    @Path("{id}/e2")
    public DataResponse<E2> getE2(@PathParam("id") int id, @Context UriInfo uriInfo) {
        return LinkRest.select(E2.class, config).parent(E3.class, id, E3.E2).uri(uriInfo).get();
    }

    @PUT
    @Path("{id}/e2/{tid}")
    public DataResponse<E2> createOrUpdate_Idempotent_E2(@PathParam("id") int parentId, @PathParam("tid") int id,
                                                         String entityData) {
        return LinkRest.idempotentCreateOrUpdate(E2.class, config).id(id).parent(E3.class, parentId, E3.E2)
                .syncAndSelect(entityData);
    }
}
