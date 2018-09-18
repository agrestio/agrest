package io.agrest.runtime.meta;

import io.agrest.AgException;
import io.agrest.annotation.LinkType;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.meta.AgResource;
import io.agrest.unit.TestWithCayenneMapping;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;

import static org.junit.Assert.*;

public class ResourceParserTest extends TestWithCayenneMapping {

    @Test(expected = Exception.class)
    public void testParse_ConflictingResourceTypes() {
         @Path("r1")
            class R1 {
                @GET
                @io.agrest.annotation.AgResource(type = LinkType.COLLECTION)
                public void method1() {}
                @GET
                @io.agrest.annotation.AgResource(type = LinkType.ITEM)
                public void method2() {}
            }

        resourceParser.parse(R1.class);
    }

    @Test
    public void testParse_UnknownEntity() {
         @Path("r1")
            class R1 {
                @GET
                @io.agrest.annotation.AgResource(entityClass = String.class)
                public void method1() {}
            }

        Collection<AgResource<?>> resources = resourceParser.parse(R1.class);
        try {
            resources.iterator().next().getEntity().getIds();
            fail("Exception expected");
        } catch (AgException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid entity '"));
        }
    }

    @Test(expected = Exception.class)
    public void testParse_ConflictingResourceEntities() {
         @Path("r1")
            class R1 {
                @GET
                @io.agrest.annotation.AgResource(entityClass = E1.class)
                public void method1() {}
                @GET
                @io.agrest.annotation.AgResource(entityClass = E2.class)
                public void method2() {}
            }

        resourceParser.parse(R1.class);
    }

    @Test
    public void testParse_NoResource() {
        Collection<AgResource<?>> resources = resourceParser.parse(Object.class);
        assertEquals(0, resources.size());
    }
}
