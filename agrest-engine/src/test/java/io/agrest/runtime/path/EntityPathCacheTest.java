package io.agrest.runtime.path;

import io.agrest.AgException;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.MetadataService;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class EntityPathCacheTest {

    private IMetadataService metadataService;

    @BeforeEach
    public void setUp() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        this.metadataService = new MetadataService(Collections.singletonList(compiler));
    }

    @Test
    public void testGetPathDescriptor_Attribute() {
        EntityPathCache cache = new EntityPathCache(metadataService.getAgEntity(X.class));
        PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("name"));
        assertNotNull(pd);
        assertTrue(pd.isAttribute());
        assertEquals(String.class, pd.getType());
        assertEquals("name", pd.getPathExp().getPath());
        assertSame(pd, cache.getPathDescriptor(new ASTObjPath("name")));
    }

    @Test
    public void testGetPathDescriptor_Relationship() {
        EntityPathCache cache = new EntityPathCache(metadataService.getAgEntity(X.class));
        PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("y"));
        assertNotNull(pd);
        assertFalse(pd.isAttribute());
        assertEquals(Y.class, pd.getType());
        assertEquals("y", pd.getPathExp().getPath());
        assertSame(pd, cache.getPathDescriptor(new ASTObjPath("y")));
    }

    @Test
    public void testGetPathDescriptor_RelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(metadataService.getAgEntity(X.class));
        PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("y.name"));
        assertNotNull(pd);
        assertTrue(pd.isAttribute());
        assertEquals(String.class, pd.getType());
        assertEquals("y.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getPathDescriptor(new ASTObjPath("y.name")));
    }

    @Test
    public void testGetPathDescriptor_BadPath() {
        EntityPathCache cache = new EntityPathCache(metadataService.getAgEntity(X.class));
        assertThrows(AgException.class, () -> cache.getPathDescriptor(new ASTObjPath("y.xyz")));
    }

    @Test
    public void testGetPathDescriptor_OuterRelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(metadataService.getAgEntity(X.class));
        PathDescriptor pd = cache.getPathDescriptor(new ASTObjPath("y+.name"));
        assertNotNull(pd);
        assertTrue(pd.isAttribute());
        assertEquals(String.class, pd.getType());
        assertEquals("y+.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getPathDescriptor(new ASTObjPath("y+.name")));
        assertNotSame(pd, cache.getPathDescriptor(new ASTObjPath("y.name")));
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
