package io.agrest.cayenne.path;

import io.agrest.AgException;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.DbAttribute;
import org.apache.cayenne.map.DbEntity;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.*;

public class EntityPathCacheTest {

    private ObjEntity x;

    @BeforeEach
    public void setUp() {

        DataMap map = new DataMap();

        DbEntity dy = new DbEntity("Y");
        dy.addAttribute(new DbAttribute("pk1", Types.INTEGER, dy));
        dy.getAttribute("pk1").setPrimaryKey(true);
        map.addDbEntity(dy);

        ObjEntity y = new ObjEntity("Y");
        y.setDbEntityName("Y");
        y.setClassName("test.Y");
        y.addAttribute(new ObjAttribute("name", "java.lang.String", x));
        map.addObjEntity(y);

        DbEntity dx = new DbEntity("X");
        dx.addAttribute(new DbAttribute("pkx1", Types.VARCHAR, dy));
        dx.getAttribute("pkx1").setPrimaryKey(true);
        map.addDbEntity(dx);

        ObjEntity x = new ObjEntity("X");
        x.setDbEntityName("X");
        x.addAttribute(new ObjAttribute("name", "java.lang.String", x));
        x.addRelationship(new ObjRelationship("y"));
        x.getRelationship("y").setTargetEntityName("Y");
        map.addObjEntity(x);

        this.x = x;
    }

    @Test
    public void getOrCreate_Attribute() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals("java.lang.String", pd.getType());
        assertEquals("name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("name"));
    }

    @Test
    public void getOrCreate_Id() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("id");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals("java.lang.Integer", pd.getType());
        assertEquals("id", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("id"));
    }

    @Test
    public void getOrCreate_Relationship() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("y");
        assertNotNull(pd);
        assertFalse(pd.isAttributeOrId());
        assertEquals("test.Y", pd.getType());
        assertEquals("y", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y"));
    }

    @Test
    public void getOrCreate_RelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("y.name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals("java.lang.String", pd.getType());
        assertEquals("y.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y.name"));
    }

    @Test
    public void getOrCreate_RelatedId() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("y.id");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals("java.lang.Integer", pd.getType());
        assertEquals("y.id", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y.id"));
    }

    @Test
    public void getOrCreate_BadPath() {
        EntityPathCache cache = new EntityPathCache(x);
        assertThrows(AgException.class, () -> cache.getOrCreate("y.xyz"));
    }

    @Test
    public void getOrCreate_OuterRelatedAttribute() {
        EntityPathCache cache = new EntityPathCache(x);
        PathDescriptor pd = cache.getOrCreate("y+.name");
        assertNotNull(pd);
        assertTrue(pd.isAttributeOrId());
        assertEquals("java.lang.String", pd.getType());
        assertEquals("y+.name", pd.getPathExp().getPath());
        assertSame(pd, cache.getOrCreate("y+.name"));
        assertNotSame(pd, cache.getOrCreate("y.name"));
    }
}
