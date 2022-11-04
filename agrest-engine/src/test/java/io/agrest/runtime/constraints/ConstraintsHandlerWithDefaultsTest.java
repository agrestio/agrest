package io.agrest.runtime.constraints;

import io.agrest.RootResourceEntity;
import io.agrest.compiler.AgEntityCompiler;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.junit.ResourceEntityUtils;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintsHandlerWithDefaultsTest {

    private static ConstraintsHandler constraintsHandler;
    private static AgSchema schema;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new AnnotationsAgEntityCompiler(Map.of());
        schema = new LazySchema(List.of(compiler));
        constraintsHandler = new ConstraintsHandler();
    }

    @Test
    public void testConstrainResponse_Default() {

        AgEntity<Tr> entity = schema.getEntity(Tr.class);

        RootResourceEntity<Tr> te1 = new RootResourceEntity<>(entity);
        ResourceEntityUtils.appendAttribute(te1, "a", Integer.class, true, true, Tr::getA);
        ResourceEntityUtils.appendAttribute(te1, "b", String.class, false, true, Tr::getB);

        constraintsHandler.constrainResponse(te1);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("a"));
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void testConstrainResponse_None() {

        AgEntity<Ts> entity = schema.getEntity(Ts.class);

        RootResourceEntity<Ts> te1 = new RootResourceEntity<>(entity);
        ResourceEntityUtils.appendAttribute(te1, "m", String.class, true, true, Ts::getM);
        ResourceEntityUtils.appendAttribute(te1, "n", String.class, true, true, Ts::getN);

        constraintsHandler.constrainResponse(te1);
        assertEquals(2, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("m"));
        assertTrue(te1.getAttributes().containsKey("n"));

        assertTrue(te1.getChildren().isEmpty());
    }

    public static class Tr {

        public int getId() {
            throw new UnsupportedOperationException();
        }

        public int getA() {
            throw new UnsupportedOperationException();
        }

        public String getB() {
            throw new UnsupportedOperationException();
        }

        public Ts getRts() {
            throw new UnsupportedOperationException();
        }
    }

    public static class Ts {

        public int getId() {
            throw new UnsupportedOperationException();
        }

        public String getN() {
            throw new UnsupportedOperationException();
        }

        public String getM() {
            throw new UnsupportedOperationException();
        }

        public List<Tr> getRtrs() {
            throw new UnsupportedOperationException();
        }
    }

}
