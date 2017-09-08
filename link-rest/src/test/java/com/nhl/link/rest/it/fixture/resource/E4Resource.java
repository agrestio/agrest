package com.nhl.link.rest.it.fixture.resource;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.LinkRest;
import com.nhl.link.rest.SelectStage;
import com.nhl.link.rest.constraints.Constraint;
import com.nhl.link.rest.it.fixture.cayenne.E4;
import com.nhl.link.rest.it.fixture.listener.CayennePaginationListener;
import com.nhl.link.rest.property.PropertyReader;
import org.apache.cayenne.Cayenne;
import org.apache.cayenne.DataObject;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Configuration;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import static com.nhl.link.rest.property.PropertyBuilder.property;

@Path("e4")
public class E4Resource {

    @Context
    private Configuration config;

    @GET
    public DataResponse<E4> get(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E4.class).uri(uriInfo).get();
    }

    /**
     * @deprecated since 2.7 in favor of {@link #get_WithPaginationStage(UriInfo)}.
     */
    @GET
    @Path("pagination_listener")
    public DataResponse<E4> get_WithPaginationListener(@Context UriInfo uriInfo) {
        return LinkRest.service(config).select(E4.class).uri(uriInfo).listener(new CayennePaginationListener())
                .get();
    }

    @GET
    @Path("pagination_stage")
    public DataResponse<E4> get_WithPaginationStage(@Context UriInfo uriInfo) {
        return LinkRest.service(config)
                .select(E4.class)
                .uri(uriInfo)
                .stage(SelectStage.APPLY_SERVER_PARAMS,
                        c -> CayennePaginationListener.RESOURCE_ENTITY_IS_FILTERED = c.getEntity().isFiltered())
                .stage(SelectStage.ASSEMBLE_QUERY,
                        c -> CayennePaginationListener.QUERY_PAGE_SIZE = c.getSelect().getPageSize())
                .get();
    }



    @GET
    @Path("calc_property")
    public DataResponse<E4> property_WithReader(@Context UriInfo uriInfo) {
        PropertyReader xReader = (root, name) -> "y_" + Cayenne.intPKForObject((DataObject) root);
        return LinkRest.select(E4.class, config).uri(uriInfo).property("x", property(xReader)).get();
    }

    @GET
    @Path("{id}")
    public DataResponse<E4> getById(@PathParam("id") int id) {
        return LinkRest.service(config).selectById(E4.class, id);
    }

    @GET
    @Path("ie/{id}")
    public DataResponse<E4> get_WithIncludeExclude(@PathParam("id") int id, @Context UriInfo uriInfo) {
        return LinkRest.service(config).selectById(E4.class, id, uriInfo);
    }
}
