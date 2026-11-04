package io.agrest.cayenne.path;

import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainNoDbTest;
import org.apache.cayenne.exp.DbPathExp;
import org.apache.cayenne.exp.ObjPathExp;
import org.apache.cayenne.exp.PathExp;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathOpsTest extends MainNoDbTest {

    @Test
    public void concat_ObjObj() {

        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new ObjPathExp("e3s"), new ObjPathExp("name"));
        assertEquals(new ObjPathExp("e3s.name"), p);
    }

    @Test
    public void concat_ObjDb() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new ObjPathExp("e3s"), new DbPathExp("name"));
        assertEquals(new DbPathExp("e3s.name"), p);
    }

    @Test
    public void concat_DbDb() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new DbPathExp("e3s"), new DbPathExp("name"));
        assertEquals(new DbPathExp("e3s.name"), p);
    }

    @Test
    public void concat_DbObj() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new DbPathExp("e3s"), new ObjPathExp("name"));
        assertEquals(new DbPathExp("e3s.name"), p);
    }

    @Test
    public void concat_ObjDb_MultiStep() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new ObjPathExp("e3s.e5"), new DbPathExp("date"));
        assertEquals(new DbPathExp("e3s.e5.date"), p);
    }

    @Test
    public void concat_ObjDb_Id() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new ObjPathExp("e3s"), new DbPathExp("_id"));
        assertEquals(new DbPathExp("e3s._id"), p);
    }

    @Test
    public void concat_ObjDb_EndsInRel() {
        ObjEntity e2 = getEntity(E2.class);
        PathExp p = PathOps.concat(e2, new ObjPathExp("e3s"), new DbPathExp("e5"));
        assertEquals(new DbPathExp("e3s.e5"), p);
    }
}
