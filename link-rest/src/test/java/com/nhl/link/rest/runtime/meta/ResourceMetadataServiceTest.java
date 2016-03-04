package com.nhl.link.rest.runtime.meta;

import static org.junit.Assert.assertEquals;

import com.nhl.link.rest.it.fixture.cayenne.E5;
import com.nhl.link.rest.it.fixture.resource.E5Resource;
import com.nhl.link.rest.meta.LinkMethodType;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrOperation;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Test;

import java.util.Collection;

public class ResourceMetadataServiceTest extends TestWithCayenneMapping {

    @Test
    public void testGetResources() {
        LrEntity<?> e5 = metadataService.getLrEntity(E5.class);
        Collection<LrResource<?>> resources = resourceMetadataService.getLrResources(E5Resource.class);

        assertEquals(3, resources.size());

        for (LrResource<?> resource : resources) {
            assertEquals(e5, resource.getEntity());

            switch (resource.getPath()) {
                case "e5": {
                    assertEquals(LinkType.COLLECTION, resource.getType());
                    assertEquals(1, resource.getOperations().size());
                    assertEquals(LinkMethodType.GET, resource.getOperations().iterator().next().getMethod());
                    break;
                }
                case "e5/{id}": {
                    assertEquals(LinkType.ITEM, resource.getType());
                    assertEquals(2, resource.getOperations().size());
                    for (LrOperation operation : resource.getOperations()) {
                        switch (operation.getMethod()) {
                            case GET:
                            case DELETE:
                                break;
                            default:
                                throw new RuntimeException(
                                        "Unexpected operation in e5 resource of type ITEM: " + operation.getMethod().name()
                                );
                        }
                    }
                    break;
                }
                case "e5/metadata": {
                    assertEquals(LinkType.METADATA, resource.getType());
                    assertEquals(1, resource.getOperations().size());
                    assertEquals(LinkMethodType.GET, resource.getOperations().iterator().next().getMethod());
                    break;
                }
                default: {
                    throw new RuntimeException("Unexpected resource: " + resource.getPath());
                }
            }
        }
    }
}
