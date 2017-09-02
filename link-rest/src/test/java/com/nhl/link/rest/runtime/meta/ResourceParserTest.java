package com.nhl.link.rest.runtime.meta;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.meta.LinkType;
import com.nhl.link.rest.meta.LrResource;
import com.nhl.link.rest.meta.annotation.Resource;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ResourceParserTest extends TestWithCayenneMapping {

    @Test(expected = Exception.class)
    public void testParse_ConflictingResourceTypes() {
         @Path("r1")
            class R1 {
                @GET
                @Resource(type = LinkType.COLLECTION)
                public void method1() {}
                @GET
                @Resource(type = LinkType.ITEM)
                public void method2() {}
            }

        resourceParser.parse(R1.class);
    }

    @Test
    public void testParse_UnknownEntity() {
         @Path("r1")
            class R1 {
                @GET
                @Resource(entityClass = String.class)
                public void method1() {}
            }

        Collection<LrResource<?>> resources = resourceParser.parse(R1.class);
        try {
            resources.iterator().next().getEntity().getIds();
            fail("Exception expected");
        } catch (LinkRestException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid entity '"));
        }
    }

    @Test(expected = Exception.class)
    public void testParse_ConflictingResourceEntities() {
         @Path("r1")
            class R1 {
                @GET
                @Resource(entityClass = E1.class)
                public void method1() {}
                @GET
                @Resource(entityClass = E2.class)
                public void method2() {}
            }

        resourceParser.parse(R1.class);
    }

    @Test
    public void testParse_NoResource() {
        Collection<LrResource<?>> resources = resourceParser.parse(Object.class);
        assertEquals(0, resources.size());
    }
}
