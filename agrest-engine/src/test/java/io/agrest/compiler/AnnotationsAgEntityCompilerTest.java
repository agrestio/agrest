package io.agrest.compiler;

import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazySchema;
import io.agrest.junit.pojo.P8;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class AnnotationsAgEntityCompilerTest {

    private static Collection<AgEntityCompiler> compilers;

    @BeforeAll
    public static void setUpClass() {
        compilers = new ArrayList<>();
        compilers.add(new AnnotationsAgEntityCompiler(Collections.emptyMap()));
    }

    @Test
    public void compile() {
        AgEntity<Entity> entity = new AnnotationsAgEntityCompiler(Collections.emptyMap())
                .compile(Entity.class, new LazySchema(compilers));
        assertNotNull(entity);
        assertEquals(1, entity.getIdParts().size());
        assertEquals(1, entity.getAttributes().size());
        assertEquals(0, entity.getRelationships().size());
    }

    @Test
    public void compile_CollectionAttributes() {
        AgEntity<P8> entity = new AnnotationsAgEntityCompiler(Collections.emptyMap())
                .compile(P8.class, new LazySchema(compilers));
        assertNotNull(entity);
        assertEquals(1, entity.getIdParts().size());
        assertEquals(7, entity.getAttributes().size());
        assertEquals(Collection.class, entity.getAttribute(P8.BOOLEANS).getType());
        assertEquals(Collection.class, entity.getAttribute(P8.DOUBLES).getType());
        assertEquals(Collection.class, entity.getAttribute(P8.CHARACTERS).getType());
        assertEquals(Collection.class, entity.getAttribute(P8.WILDCARD_COLLECTION).getType());
        assertEquals(Collection.class, entity.getAttribute(P8.GENERIC_COLLECTION).getType());
        assertEquals(List.class, entity.getAttribute(P8.NUMBER_LIST).getType());
        assertEquals(Set.class, entity.getAttribute(P8.STRING_SET).getType());
        assertEquals(0, entity.getRelationships().size());
    }

    @Test
    public void compile_EntityWithNoAnnotations() {
        AgEntity<EntityNoAnnotations> entity = new AnnotationsAgEntityCompiler(Collections.emptyMap())
                .compile(EntityNoAnnotations.class, new LazySchema(compilers));
        assertNotNull(entity);
        assertTrue(entity.getAttributes().isEmpty());
    }

    static class Entity {

        @AgId
        public String getX() {
            return "x";
        }

        @AgAttribute
        public int getY() {
            return 6;
        }
    }

    static class EntityNoAnnotations {

        public String getX() {
            return "x";
        }

        public int getY() {
            return 6;
        }
    }
}
