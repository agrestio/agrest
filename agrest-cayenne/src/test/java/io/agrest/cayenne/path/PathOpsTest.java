package io.agrest.cayenne.path;

import io.agrest.cayenne.cayenne.main.E2;
import io.agrest.cayenne.unit.main.MainNoDbTest;
import org.apache.cayenne.exp.parser.ASTDbPath;
import org.apache.cayenne.exp.parser.ASTObjPath;
import org.apache.cayenne.exp.parser.ASTPath;
import org.apache.cayenne.map.ObjEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PathOpsTest extends MainNoDbTest {

    @Test
    public void concat_ObjObj() {

        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTObjPath("e3s"), new ASTObjPath("name"));
        assertEquals(new ASTObjPath("e3s.name"), p);
    }

    @Test
    public void concat_ObjDb() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTObjPath("e3s"), new ASTDbPath("name"));
        assertEquals(new ASTDbPath("e3s.name"), p);
    }

    @Test
    public void concat_DbDb() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTDbPath("e3s"), new ASTDbPath("name"));
        assertEquals(new ASTDbPath("e3s.name"), p);
    }

    @Test
    public void concat_DbObj() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTDbPath("e3s"), new ASTObjPath("name"));
        assertEquals(new ASTDbPath("e3s.name"), p);
    }

    @Test
    public void concat_ObjDb_MultiStep() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTObjPath("e3s.e5"), new ASTDbPath("date"));
        assertEquals(new ASTDbPath("e3s.e5.date"), p);
    }

    @Test
    public void concat_ObjDb_Id() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTObjPath("e3s"), new ASTDbPath("_id"));
        assertEquals(new ASTDbPath("e3s._id"), p);
    }

    @Test
    public void concat_ObjDb_EndsInRel() {
        ObjEntity e2 = getEntity(E2.class);
        ASTPath p = PathOps.concat(e2, new ASTObjPath("e3s"), new ASTDbPath("e5"));
        assertEquals(new ASTDbPath("e3s.e5"), p);
    }
}
