package io.agrest.runtime.meta;

import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.annotation.LinkType;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.LinkMethodType;
import io.agrest.meta.LrEntity;
import io.agrest.meta.LrOperation;
import io.agrest.meta.LrResource;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Assert;
import org.junit.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ResourceMetadataServiceTest extends TestWithCayenneMapping {

    @Test
    public void testGetResources() {
        LrEntity<?> e5 = metadataService.getLrEntity(E5.class);
        Collection<LrResource<?>> resources = resourceMetadataService.getLrResources(E5Resource.class);

        assertEquals(4, resources.size());

        for (LrResource<?> resource : resources) {
            assertEquals(e5, resource.getEntity());

            switch (resource.getPath()) {
                case "e5":
                    Assert.assertEquals(LinkType.COLLECTION, resource.getType());
                    assertEquals(1, resource.getOperations().size());
                    Assert.assertEquals(LinkMethodType.GET, resource.getOperations().iterator().next().getMethod());
                    break;
                case "e5/{id}":
                    assertEquals(LinkType.ITEM, resource.getType());
                    assertEquals(2, resource.getOperations().size());
                    for (LrOperation operation : resource.getOperations()) {
                        switch (operation.getMethod()) {
                            case GET:
                            case DELETE:
                                break;
                            default:
                                fail("Unexpected operation of type ITEM: " + operation.getMethod().name());
                                break;
                        }
                    }
                    break;
                case "e5/md1":
                case "e5/md2":
                    assertEquals(LinkType.METADATA, resource.getType());
                    assertEquals(1, resource.getOperations().size());
                    assertEquals(LinkMethodType.GET, resource.getOperations().iterator().next().getMethod());
                    break;
                default: {
                    fail("Unexpected resource: " + resource.getPath());
                }
            }
        }
    }

    @Path("e5")
    public static class E5Resource {

        @GET
        @io.agrest.annotation.LrResource(type = LinkType.COLLECTION)
        public DataResponse<E5> get(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("{id}")
        @io.agrest.annotation.LrResource(type = LinkType.ITEM)
        public DataResponse<E5> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @DELETE
        @Path("{id}")
        public SimpleResponse delete(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("md1")
        @io.agrest.annotation.LrResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> md1(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("md2")
        @io.agrest.annotation.LrResource(entityClass = E5.class, type = LinkType.METADATA)
        public MetadataResponse<E5> md2(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }
    }

}
