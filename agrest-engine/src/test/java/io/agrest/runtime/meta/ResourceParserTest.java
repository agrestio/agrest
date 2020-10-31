package io.agrest.runtime.meta;

import io.agrest.annotation.LinkType;
import io.agrest.meta.AgResource;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.meta.parser.ResourceParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ResourceParserTest {

    private static ResourceParser resourceParser;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        MetadataService metadata = new MetadataService(Collections.singletonList(compiler));
        resourceParser = new ResourceParser(metadata);
    }

    @Test
    public void testParse_ConflictingResourceTypes() {
        @Path("r1")
        class R1 {
            @GET
            @io.agrest.annotation.AgResource(type = LinkType.COLLECTION)
            public void method1() {
            }

            @GET
            @io.agrest.annotation.AgResource(type = LinkType.ITEM)
            public void method2() {
            }
        }

        assertThrows(Exception.class, () -> resourceParser.parse(R1.class));
    }

    @Test
    public void testParse_ConflictingResourceEntities() {
        @Path("r1")
        class R1 {
            @GET
            @io.agrest.annotation.AgResource(entityClass = Tr.class)
            public void method1() {
            }

            @GET
            @io.agrest.annotation.AgResource(entityClass = Ts.class)
            public void method2() {
            }
        }

        assertThrows(Exception.class, () -> resourceParser.parse(R1.class));
    }

    @Test
    public void testParse_NoResource() {
        Collection<AgResource<?>> resources = resourceParser.parse(Object.class);
        assertEquals(0, resources.size());
    }

    public static class Tr {

    }

    public static class Ts {

    }
}
