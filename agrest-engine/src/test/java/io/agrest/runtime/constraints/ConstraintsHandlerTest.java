package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgDataMap;
import io.agrest.meta.AgEntity;
import io.agrest.meta.LazyAgDataMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConstraintsHandlerTest {

    private static ConstraintsHandler constraintsHandler;
    private static AgDataMap dataMap;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Collections.emptyMap());
        dataMap = new LazyAgDataMap(Collections.singletonList(compiler));
        constraintsHandler = new ConstraintsHandler();
    }

    @Test
    public void testConstrainResponse_FetchOffset() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);

        SizeConstraints s1 = new SizeConstraints().fetchOffset(5);
        SizeConstraints s2 = new SizeConstraints().fetchOffset(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity);
        t1.setStart(0);
        constraintsHandler.constrainResponse(t1, s1);
        assertEquals(0, t1.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity);
        t2.setStart(3);
        constraintsHandler.constrainResponse(t2, s1);
        assertEquals(3, t2.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity);
        t3.setStart(6);
        constraintsHandler.constrainResponse(t3, s1);
        assertEquals(5, t3.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity);
        t4.setStart(6);
        constraintsHandler.constrainResponse(t4, s2);
        assertEquals(6, t4.getStart());
        assertEquals(0, s2.getFetchOffset());
    }

    @Test
    public void testConstrainResponse_FetchLimit() {

        AgEntity<Tr> entity = dataMap.getEntity(Tr.class);

        SizeConstraints s1 = new SizeConstraints().fetchLimit(5);
        SizeConstraints s2 = new SizeConstraints().fetchLimit(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity);
        constraintsHandler.constrainResponse(t1, s1);
        assertEquals(5, t1.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_1 = new RootResourceEntity<>(entity);
        t1_1.setLimit(0);
        constraintsHandler.constrainResponse(t1_1, s1);
        assertEquals(5, t1_1.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_2 = new RootResourceEntity<>(entity);
        t1_2.setLimit(-1);
        constraintsHandler.constrainResponse(t1_2, s1);
        assertEquals(5, t1_2.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity);
        t2.setLimit(3);
        constraintsHandler.constrainResponse(t2, s1);
        assertEquals(3, t2.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity);
        t3.setLimit(6);
        constraintsHandler.constrainResponse(t3, s1);
        assertEquals(5, t3.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity);
        t4.setLimit(6);
        constraintsHandler.constrainResponse(t4, s2);
        assertEquals(6, t4.getLimit());
        assertEquals(0, s2.getFetchLimit());
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
