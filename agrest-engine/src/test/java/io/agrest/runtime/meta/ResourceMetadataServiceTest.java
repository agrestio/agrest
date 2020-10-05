package io.agrest.runtime.meta;

import io.agrest.DataResponse;
import io.agrest.MetadataResponse;
import io.agrest.SimpleResponse;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.LinkType;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgOperation;
import io.agrest.meta.AgResource;
import io.agrest.meta.LinkMethodType;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.meta.parser.ResourceParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ResourceMetadataServiceTest {

    private static IMetadataService metadata;
    private static IResourceMetadataService resourceMetadata;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        metadata = new MetadataService(Collections.singletonList(compiler));
        resourceMetadata = new ResourceMetadataService(
                new ResourceParser(metadata),
                BaseUrlProvider.forUrl(Optional.empty()));
    }

    @Test
    public void testGetResources() {
        AgEntity<Tr> entity = metadata.getAgEntity(Tr.class);
        Collection<AgResource<?>> resources = resourceMetadata.getAgResources(E5Resource.class);

        assertEquals(4, resources.size());

        for (AgResource<?> resource : resources) {
            assertEquals(entity, resource.getEntity());

            switch (resource.getPath()) {
                case "tr":
                    assertEquals(LinkType.COLLECTION, resource.getType());
                    assertEquals(1, resource.getOperations().size());
                    assertEquals(LinkMethodType.GET, resource.getOperations().iterator().next().getMethod());
                    break;
                case "tr/{id}":
                    assertEquals(LinkType.ITEM, resource.getType());
                    assertEquals(2, resource.getOperations().size());
                    for (AgOperation operation : resource.getOperations()) {
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
                case "tr/md1":
                case "tr/md2":
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

    @Path("tr")
    public static class E5Resource {

        @GET
        @io.agrest.annotation.AgResource(type = LinkType.COLLECTION)
        public DataResponse<Tr> get(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("{id}")
        @io.agrest.annotation.AgResource(type = LinkType.ITEM)
        public DataResponse<Tr> getById(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @DELETE
        @Path("{id}")
        public SimpleResponse delete(@PathParam("id") int id, @Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("md1")
        @io.agrest.annotation.AgResource(entityClass = Tr.class, type = LinkType.METADATA)
        public MetadataResponse<Tr> md1(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }

        @GET
        @Path("md2")
        @io.agrest.annotation.AgResource(entityClass = Tr.class, type = LinkType.METADATA)
        public MetadataResponse<Tr> md2(@Context UriInfo uriInfo) {
            throw new UnsupportedOperationException("Response is not relevant here");
        }
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }
    }

}
