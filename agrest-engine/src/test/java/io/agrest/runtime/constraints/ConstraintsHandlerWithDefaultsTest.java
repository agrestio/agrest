package io.agrest.runtime.constraints;

import io.agrest.EntityConstraint;
import io.agrest.RootResourceEntity;
import io.agrest.annotation.AgAttribute;
import io.agrest.annotation.AgId;
import io.agrest.annotation.AgRelationship;
import io.agrest.constraints.Constraint;
import io.agrest.meta.AgEntity;
import io.agrest.meta.compiler.AgEntityCompiler;
import io.agrest.meta.compiler.PojoEntityCompiler;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.meta.MetadataService;
import io.agrest.unit.ResourceEntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConstraintsHandlerWithDefaultsTest {

    private static ConstraintsHandler constraintsHandler;
    private static IMetadataService metadata;

    @BeforeAll
    public static void before() {
        AgEntityCompiler compiler = new PojoEntityCompiler(Collections.emptyMap());
        metadata = new MetadataService(Collections.singletonList(compiler));

        List<EntityConstraint> r = Collections.singletonList(
                new DefaultEntityConstraint("Tr", true, false, Collections.singleton("a"), Collections.emptySet()));

        List<EntityConstraint> w = Collections.singletonList(
                new DefaultEntityConstraint("Ts", false, false, Collections.singleton("n"), Collections.emptySet()));

        constraintsHandler = new ConstraintsHandler(r, w);
    }

    @Test
    public void testConstrainResponse_PerRequest() {

        AgEntity<Tr> entity = metadata.getAgEntity(Tr.class);
        Constraint<Tr> tc1 = Constraint.excludeAll(Tr.class).attributes("b");

        RootResourceEntity<Tr> te1 = new RootResourceEntity<>(entity, null);
        ResourceEntityUtils.appendAttribute(te1, "a", Integer.class);
        ResourceEntityUtils.appendAttribute(te1, "b", String.class);

        constraintsHandler.constrainResponse(te1, null, tc1);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("b"));
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void testConstrainResponse_Default() {

        AgEntity<Tr> entity = metadata.getAgEntity(Tr.class);

        RootResourceEntity<Tr> te1 = new RootResourceEntity<>(entity, null);
        ResourceEntityUtils.appendAttribute(te1, "a", Integer.class);
        ResourceEntityUtils.appendAttribute(te1, "b", String.class);

        constraintsHandler.constrainResponse(te1, null, null);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("a"));
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void testConstrainResponse_None() {

        AgEntity<Ts> entity = metadata.getAgEntity(Ts.class);

        RootResourceEntity<Ts> te1 = new RootResourceEntity<>(entity, null);
        ResourceEntityUtils.appendAttribute(te1, "m", String.class);
        ResourceEntityUtils.appendAttribute(te1, "n", String.class);

        constraintsHandler.constrainResponse(te1, null, null);
        assertEquals(2, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("m"));
        assertTrue(te1.getAttributes().containsKey("n"));

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

        @AgAttribute
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
