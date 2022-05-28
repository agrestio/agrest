package io.agrest.cayenne.path;

import io.agrest.AgException;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class EntityPathCacheTest {

    private AgSchema schema;

    @BeforeEach
    public void setUp() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        this.schema = new LazySchema(Collections.singletonList(compiler));
    }

    @Test
    public void testGetOrCreate_Attribute() {
        EntityPathCache cache = new EntityPathCache(schema.getEntity(X.class));
        PathDescriptor pd = cache.getOrCreate("name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals(String.class, pd.getType());
        assertEquals("name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("name"));
    }

    @Test
    public void testGetOrCreate_Relationship() {
        EntityPathCache cache = new EntityPathCache(schema.getEntity(X.class));
        PathDescriptor pd = cache.getOrCreate("y");
        assertNotNull(pd);
        assertFalse(pd.isAttributeOrId());
        assertEquals(Y.class, pd.getType());
        assertEquals("y", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y"));
    }

    @Test
    public void testGetOrCreate_RelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(schema.getEntity(X.class));
        PathDescriptor pd = cache.getOrCreate("y.name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals(String.class, pd.getType());
        assertEquals("y.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y.name"));
    }

    @Test
    public void tesGetOrCreate_BadPath() {
        EntityPathCache cache = new EntityPathCache(schema.getEntity(X.class));
        assertThrows(AgException.class, () -> cache.getOrCreate("y.xyz"));
    }

    @Test
    public void testGetOrCreate_OuterRelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(schema.getEntity(X.class));
        PathDescriptor pd = cache.getOrCreate("y+.name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals(String.class, pd.getType());
        assertEquals("y+.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y+.name"));
        assertNotSame(pd, cache.getOrCreate("y.name"));
    }

    public static class X {

        @AgAttribute
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Y getY() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Y {
        @AgAttribute
        public String getName() {
            throw new UnsupportedOperationException();
        }
    }
}
