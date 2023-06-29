package io.agrest.runtime.constraints;

import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SizeConstraintsTest {

    private static AgSchema schema;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Map.of());
        schema = new LazySchema(List.of(compiler));
    }

    @Test
    public void fetchOffset() {

        AgEntity<Tr> entity = schema.getEntity(Tr.class);

        io.agrest.SizeConstraints s1 = new io.agrest.SizeConstraints().fetchOffset(5);
        io.agrest.SizeConstraints s2 = new io.agrest.SizeConstraints().fetchOffset(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity);
        t1.setStart(0);
        SizeConstraints.apply(t1, s1);
        assertEquals(0, t1.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity);
        t2.setStart(3);
        SizeConstraints.apply(t2, s1);
        assertEquals(3, t2.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity);
        t3.setStart(6);
        SizeConstraints.apply(t3, s1);
        assertEquals(5, t3.getStart());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity);
        t4.setStart(6);
        SizeConstraints.apply(t4, s2);
        assertEquals(6, t4.getStart());
        assertEquals(0, s2.getFetchOffset());
    }

    @Test
    public void fetchLimit() {

        AgEntity<Tr> entity = schema.getEntity(Tr.class);

        io.agrest.SizeConstraints s1 = new io.agrest.SizeConstraints().fetchLimit(5);
        io.agrest.SizeConstraints s2 = new io.agrest.SizeConstraints().fetchLimit(0);

        ResourceEntity<Tr> t1 = new RootResourceEntity<>(entity);
        SizeConstraints.apply(t1, s1);
        assertEquals(5, t1.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_1 = new RootResourceEntity<>(entity);
        t1_1.setLimit(0);
        SizeConstraints.apply(t1_1, s1);
        assertEquals(5, t1_1.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t1_2 = new RootResourceEntity<>(entity);
        t1_2.setLimit(-1);
        SizeConstraints.apply(t1_2, s1);
        assertEquals(5, t1_2.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t2 = new RootResourceEntity<>(entity);
        t2.setLimit(3);
        SizeConstraints.apply(t2, s1);
        assertEquals(3, t2.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t3 = new RootResourceEntity<>(entity);
        t3.setLimit(6);
        SizeConstraints.apply(t3, s1);
        assertEquals(5, t3.getLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<Tr> t4 = new RootResourceEntity<>(entity);
        t4.setLimit(6);
        SizeConstraints.apply(t4, s2);
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
