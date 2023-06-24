package io.agrest.runtime.constraints;

import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.compiler.AnnotationsAgEntityCompiler;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgSchema;
import io.agrest.meta.LazySchema;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintsHandlerWithDefaultsTest {

    static final ConstraintsHandler constraintsHandler = new ConstraintsHandler();
    static final AgSchema schema = new LazySchema(List.of(new AnnotationsAgEntityCompiler(Map.of())));

    @Test
    public void constrainResponse_Default() {

        AgEntity<Tr> entity = schema.getEntity(Tr.class);

        RootResourceEntity<Tr> te1 = new RootResourceEntity<>(entity);
        te1.ensureAttribute("a", false);
        te1.ensureAttribute("b", false);

        constraintsHandler.constrainResponse(te1);
        assertEquals(1, te1.getBaseProjection().getAttributes().size());
        assertTrue(te1.getBaseProjection().getAttribute("a") != null);
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void constrainResponse_None() {

        AgEntity<Ts> entity = schema.getEntity(Ts.class);

        RootResourceEntity<Ts> te1 = new RootResourceEntity<>(entity);
        te1.ensureAttribute("m", false);
        te1.ensureAttribute("n", false);

        constraintsHandler.constrainResponse(te1);
        assertEquals(2, te1.getBaseProjection().getAttributes().size());
        assertTrue(te1.getBaseProjection().getAttribute("m") != null);
        assertTrue(te1.getBaseProjection().getAttribute("n") != null);

        assertTrue(te1.getChildren().isEmpty());
    }

    public static class Tr {

        @AgId
        public int getId() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute
        public int getA() {
            throw new UnsupportedOperationException();
        }

        @AgAttribute(readable = false)
        public String getB() {
            throw new UnsupportedOperationException();
        }

        @AgRelationship
        public Ts getRts() {
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

        @AgRelationship
        public List<Tr> getRtrs() {
            throw new UnsupportedOperationException();
        }
    }

}
