package io.agrest.runtime.constraints;

import io.agrest.NestedResourceEntity;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.base.protocol.Exp;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.constraints.Constraint;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import io.agrest.unit.ResourceEntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConstraintsHandlerTest {

    private static ConstraintsHandler constraintsHandler;
    private static AgDataMap dataMap;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        constraintsHandler = new ConstraintsHandler(Collections.emptyList(), Collections.emptyList());
    }

    @Test
    public void testConstrainResponse_FetchOffset() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);

        SizeConstraints s1 = new SizeConstraints().fetchOffset(5);
        SizeConstraints s2 = new SizeConstraints().fetchOffset(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity, null);
        t1.setFetchOffset(0);
        constraintsHandler.constrainResponse(t1, s1, null);
        assertEquals(0, t1.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity, null);
        t2.setFetchOffset(3);
        constraintsHandler.constrainResponse(t2, s1, null);
        assertEquals(3, t2.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity, null);
        t3.setFetchOffset(6);
        constraintsHandler.constrainResponse(t3, s1, null);
        assertEquals(5, t3.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity, null);
        t4.setFetchOffset(6);
        constraintsHandler.constrainResponse(t4, s2, null);
        assertEquals(6, t4.getFetchOffset());
        assertEquals(0, s2.getFetchOffset());
    }

    @Test
    public void testConstrainResponse_FetchLimit() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);

        SizeConstraints s1 = new SizeConstraints().fetchLimit(5);
        SizeConstraints s2 = new SizeConstraints().fetchLimit(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity, null);
        constraintsHandler.constrainResponse(t1, s1, null);
        assertEquals(5, t1.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_1 = new RootResourceEntity<>(entity, null);
        t1_1.setFetchLimit(0);
        constraintsHandler.constrainResponse(t1_1, s1, null);
        assertEquals(5, t1_1.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_2 = new RootResourceEntity<>(entity, null);
        t1_2.setFetchLimit(-1);
        constraintsHandler.constrainResponse(t1_2, s1, null);
        assertEquals(5, t1_2.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity, null);
        t2.setFetchLimit(3);
        constraintsHandler.constrainResponse(t2, s1, null);
        assertEquals(3, t2.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity, null);
        t3.setFetchLimit(6);
        constraintsHandler.constrainResponse(t3, s1, null);
        assertEquals(5, t3.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity, null);
        t4.setFetchLimit(6);
        constraintsHandler.constrainResponse(t4, s2, null);
        assertEquals(6, t4.getFetchLimit());
        assertEquals(0, s2.getFetchLimit());
    }

    @Test
    public void testConstrainResponse_ResourceEntity_NoTargetRel() {

        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);

        Constraint<Tr> tc1 = Constraint.excludeAll(Tr.class).attributes("a", "b");

        ResourceEntity<Tr> te1 = new RootResourceEntity<>(entityTr, null);
        ResourceEntityUtils.appendAttribute(te1, "c", String.class, Tr::getC);
        ResourceEntityUtils.appendAttribute(te1, "b", String.class, Tr::getB);

        NestedResourceEntity<Ts> te11 = new NestedResourceEntity<>(entityTs, null, te1, entityTr.getRelationship("rts"));
        ResourceEntityUtils.appendAttribute(te11, "n", String.class, Ts::getN);
        ResourceEntityUtils.appendAttribute(te11, "z", String.class, Ts::getZ);
        te1.getChildren().put("d", te11);

        constraintsHandler.constrainResponse(te1, null, tc1);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("b"));
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void testConstrainResponse_ResourceEntity_TargetRel() {

        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);
        AgEntity<Tv> entityTv = dataMap.getEntity(Tv.class);

        Constraint<Tr> constraint = Constraint.excludeAll(Tr.class).attributes("a", "b")
                .path("rts", Constraint.excludeAll(Ts.class).attributes("n", "m"))
                .path("rts.rtt", Constraint.excludeAll(Tt.class).attributes("p", "r"))
                .path("rtu", Constraint.excludeAll(Tu.class).attributes("k", "l"));

        ResourceEntity<Tr> tr = new RootResourceEntity<>(entityTr, null);
        ResourceEntityUtils. appendAttribute(tr, "c", String.class, Tr::getC);
        ResourceEntityUtils.appendAttribute(tr, "b", String.class, Tr::getB);

        NestedResourceEntity<Ts> ts = new NestedResourceEntity<>(entityTs, null, tr, entityTr.getRelationship("rts"));
        ResourceEntityUtils.appendAttribute(ts, "m", String.class, Ts::getM);
        ResourceEntityUtils.appendAttribute(ts, "z", String.class, Ts::getZ);
        tr.getChildren().put("rts", ts);

        NestedResourceEntity<Tv> tv = new NestedResourceEntity<>(entityTv, null, tr, entityTr.getRelationship("rtv"));
        ResourceEntityUtils.appendAttribute(tv, "p", String.class, Tv::getP);
        ResourceEntityUtils.appendAttribute(tv, "z", String.class, Tv::getZ);
        tr.getChildren().put("rtv", tv);

        constraintsHandler.constrainResponse(tr, null, constraint);
        assertEquals(1, tr.getAttributes().size());
        assertTrue(tr.getAttributes().containsKey("b"));
        assertEquals(1, tr.getChildren().size());

        ResourceEntity<?> mergedTs = tr.getChildren().get("rts");
        assertNotNull(mergedTs);
        assertTrue(mergedTs.getChildren().isEmpty());
        assertEquals(1, mergedTs.getAttributes().size());
        assertTrue(mergedTs.getAttributes().containsKey("m"));
    }

    @Test
    public void testConstrainResponse_ResourceEntity_Id() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);

        Constraint<Tr> constraint1 = Constraint.excludeAll(Tr.class).excludeId();
        Constraint<Tr> constraint2 = Constraint.excludeAll(Tr.class).includeId();

        ResourceEntity<Tr> e1 = new RootResourceEntity<>(entity, null);
        e1.includeId();
        constraintsHandler.constrainResponse(e1, null, constraint1);
        assertFalse(e1.isIdIncluded());

        ResourceEntity<Tr> e2 = new RootResourceEntity<>(entity, null);
        e2.includeId();
        constraintsHandler.constrainResponse(e2, null, constraint2);
        assertTrue(e2.isIdIncluded());

        ResourceEntity<Tr> e3 = new RootResourceEntity<>(entity, null);
        e3.excludeId();
        constraintsHandler.constrainResponse(e3, null, constraint2);
        assertFalse(e3.isIdIncluded());
    }

    @Test
    public void testConstrainResponse_Exp() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);
        Constraint<Tr> constraint = Constraint.excludeAll(Tr.class).qualifier(new Exp("a = 5"));

        ResourceEntity<Tr> e1 = new RootResourceEntity<>(entity, null);
        constraintsHandler.constrainResponse(e1, null, constraint);
        assertEquals(1, e1.getQualifiers().size());
        assertEquals(new Exp("a = 5"), e1.getQualifiers().get(0));
    }

    @Test
    public void testConstrainResponse_MapByAttribute_Excluded() {

        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);

        Constraint<Tr> constraint = Constraint
                .excludeAll(Tr.class)
                .path("rts", Constraint.excludeAll(Ts.class).attribute("m"));

        ResourceEntity<Ts> tsMapBy = new RootResourceEntity<>(entityTs, null);
        ResourceEntityUtils.appendAttribute(tsMapBy, "m", String.class, Ts::getM);
        ResourceEntityUtils.appendAttribute(tsMapBy, "n", String.class, Ts::getN);

        NestedResourceEntity<Tr> trMapBy = new NestedResourceEntity<>(entityTr, null, tsMapBy, entityTr.getRelationship("rts"));
        ResourceEntityUtils.appendAttribute(trMapBy, "a", String.class, Tr::getA);
        ResourceEntityUtils.appendAttribute(trMapBy, "b", String.class, Tr::getB);
        tsMapBy.getChildren().put("rts", trMapBy);

        ResourceEntity<Tr> e = new RootResourceEntity<>(entityTr, null);
        e.mapBy(tsMapBy, "rts.n");

        constraintsHandler.constrainResponse(e, null, constraint);
        assertNull(e.getMapBy());
        assertNull(e.getMapByPath());
    }

    @Test
    public void testConstrainResponse_MapByAttribute_Included() {
        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);

        Constraint<Tr> constraint = Constraint
                .excludeAll(Tr.class)
                .path("rts", Constraint.excludeAll(Ts.class).attribute("m"));

        ResourceEntity<Ts> tsMapBy = new RootResourceEntity<>(entityTs, null);
        ResourceEntityUtils.appendAttribute(tsMapBy, "m", String.class, Ts::getM);
        ResourceEntityUtils.appendAttribute(tsMapBy, "n", String.class, Ts::getN);

        NestedResourceEntity<Tr> trMapBy = new NestedResourceEntity<>(entityTr, null, tsMapBy, entityTs.getRelationship("rtrs"));
        ResourceEntityUtils.appendAttribute(trMapBy, "a", String.class, Tr::getA);
        ResourceEntityUtils.appendAttribute(trMapBy, "b", String.class, Tr::getB);
        tsMapBy.getChildren().put("rts", trMapBy);

        ResourceEntity<Tr> e = new RootResourceEntity<>(entityTr, null);
        e.mapBy(tsMapBy, "rts.m");

        constraintsHandler.constrainResponse(e, null, constraint);
        assertSame(tsMapBy, e.getMapBy());
        assertEquals("rts.m", e.getMapByPath());
    }

    @Test
    public void testConstrainResponse_MapById_Excluded() {

        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);

        Constraint<Tr> constraint = Constraint
                .excludeAll(Tr.class)
                .path("rts", Constraint.excludeAll(Ts.class).excludeId());

        ResourceEntity<Ts> te1MapBy = new RootResourceEntity<>(entityTs, null);
        NestedResourceEntity<Tr> te1MapByTarget = new NestedResourceEntity<>(entityTr, null, te1MapBy, entityTs.getRelationship("rtrs"));
        te1MapByTarget.includeId();

        te1MapBy.getChildren().put("rtrs", te1MapByTarget);

        ResourceEntity<Tr> te1 = new RootResourceEntity<>(entityTr, null);
        te1.mapBy(te1MapBy, "rts");

        constraintsHandler.constrainResponse(te1, null, constraint);
        assertNull(te1.getMapBy());
        assertNull(te1.getMapByPath());

    }

    @Test
    public void testConstrainResponse_MapById_Included() {

        AgEntity<Tr> entityTr = dataMap.getEntity(Tr.class);
        AgEntity<Ts> entityTs = dataMap.getEntity(Ts.class);

        Constraint<Tr> constraint = Constraint
                .excludeAll(Tr.class)
                .path("rts", Constraint.excludeAll(Ts.class).includeId());

        ResourceEntity<Tr> te1MapBy = new RootResourceEntity<>(entityTr, null);
        NestedResourceEntity<Ts> te1MapByTarget = new NestedResourceEntity<>(entityTs, null, te1MapBy, entityTr.getRelationship("rts"));
        te1MapByTarget.includeId();
        te1MapBy.getChildren().put("rts", te1MapByTarget);

        ResourceEntity<Tr> te1 = new RootResourceEntity<>(entityTr, null);
        te1.mapBy(te1MapBy, "rts");

        constraintsHandler.constrainResponse(te1, null, constraint);
        assertSame(te1MapBy, te1.getMapBy());
        assertEquals("rts", te1.getMapByPath());
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getB() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getC() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Ts getRts() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Tu getRtu() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Tv getRtv() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Ts {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getN() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getM() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getZ() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Tt getRtt() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public List<Tr> getRtrs() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tt {
        @AgAttribute
        public String getP() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getR() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tu {

        @AgAttribute
        public String getK() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getL() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Tv {

        @AgAttribute
        public String getP() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public String getZ() {
            throw new UnsupportedOperationException();
        }
    }
}
