package com.nhl.link.rest.it.fixture.resource;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.MatrixParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityDelete;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.it.fixture.cayenne.E17;
import com.nhl.link.rest.it.fixture.cayenne.E18;

@Path("e17")
public class E17Resource {

    @Context
	private Configuration config;

    @GET
	public DataResponse<E17> getById(@Context UriInfo uriInfo, @QueryParam("id1") Integer id1,
                                     @QueryParam("id2") Integer id2) {

        Map<String, Object> ids = new HashMap<>();
        ids.put(E17.ID1_PK_COLUMN, id1);
        ids.put(E17.ID2_PK_COLUMN, id2);

		return LinkRest.select(E17.class, config).uri(uriInfo).byId(ids).selectOne();
	}

    @GET
	@Path("e18s")
	public DataResponse<E18> getChildren(@Context UriInfo uriInfo, @MatrixParam("parentId1") Integer parentId1,
                                      @MatrixParam("parentId2") Integer parentId2) {

        Map<String, Object> parentIds = new HashMap<>();
        parentIds.put(E17.ID1_PK_COLUMN, parentId1);
        parentIds.put(E17.ID2_PK_COLUMN, parentId2);

		return LinkRest.select(E18.class, config).parent(E17.class, parentIds, E17.E18S.getName()).uri(uriInfo).select();
	}

    @POST
	public DataResponse<E17> create(@Context UriInfo uriInfo, @QueryParam("id1") Integer id1,
                                     @QueryParam("id2") Integer id2, String requestBody) {

        Map<String, Object> ids = new HashMap<>();
        ids.put(E17.ID1_PK_COLUMN, id1);
        ids.put(E17.ID2_PK_COLUMN, id2);

		return LinkRest.create(E17.class, config).id(ids).syncAndSelect(requestBody);
	}

    @POST
	@Path("e18s")
	public DataResponse<E18> createOrUpdateE18s(@Context UriInfo uriInfo, @MatrixParam("parentId1") Integer parentId1,
                                      @MatrixParam("parentId2") Integer parentId2, String targetData) {

        Map<String, Object> parentIds = new HashMap<>();
        parentIds.put(E17.ID1_PK_COLUMN, parentId1);
        parentIds.put(E17.ID2_PK_COLUMN, parentId2);

		return LinkRest.createOrUpdate(E18.class, config).toManyParent(E17.class, parentIds, E17.E18S).uri(uriInfo)
                .syncAndSelect(targetData);
	}

    @PUT
	public DataResponse<E17> updateById(@Context UriInfo uriInfo, @QueryParam("id1") Integer id1,
                                     @QueryParam("id2") Integer id2, String targetData) {

        Map<String, Object> ids = new HashMap<>();
        ids.put(E17.ID1_PK_COLUMN, id1);
        ids.put(E17.ID2_PK_COLUMN, id2);

		return LinkRest.update(E17.class, config).uri(uriInfo).id(ids).syncAndSelect(targetData);
	}

    @DELETE
	public SimpleResponse delete(@Context UriInfo uriInfo, @QueryParam("id1") Integer id1,
                                 @QueryParam("id2") Integer id2) {

        Map<String, Object> ids = new HashMap<>();
        ids.put(E17.ID1_PK_COLUMN, id1);
        ids.put(E17.ID2_PK_COLUMN, id2);

		return LinkRest.service(config).delete(E17.class, ids);
	}

    @DELETE
    @Path("batch_delete")
    public SimpleResponse delete_Batch(Collection<EntityDelete<E17>> deleted) {
        return LinkRest.service(config).delete(E17.class, deleted);
    }
}
