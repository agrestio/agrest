package io.agrest.runtime.constraints;

import io.agrest.ChildResourceEntity;
import io.agrest.EntityConstraint;
import io.agrest.ResourceEntity;
import io.agrest.RootResourceEntity;
import io.agrest.SizeConstraints;
import io.agrest.constraints.Constraint;
import io.agrest.it.fixture.cayenne.E1;
import io.agrest.it.fixture.cayenne.E2;
import io.agrest.it.fixture.cayenne.E3;
import io.agrest.it.fixture.cayenne.E4;
import io.agrest.it.fixture.cayenne.E5;
import io.agrest.meta.AgEntity;
import io.agrest.meta.DefaultAgAttribute;
import io.agrest.meta.AgRelationship;
import io.agrest.property.BeanPropertyReader;
import org.apache.cayenne.exp.Expression;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConstraintsHandlerTest {

    private ConstraintsHandler constraintHandler;

    private AgEntity<E1> age0;
    private AgEntity<E2> age1;
    private AgEntity<E3> age2;
    private AgEntity<E4> age3;
    private AgEntity<E5> age4;

    @SuppressWarnings("unchecked")
    @Before
    public void before() {

        age0 = mock(AgEntity.class);
        when(age0.getName()).thenReturn("E1");
        when(age0.getType()).thenReturn(E1.class);
        AgRelationship r1 = mock(AgRelationship.class);
        when(age0.getRelationship("r1")).thenReturn(r1);
        when(r1.getName()).thenReturn("r1");
        when(r1.getTargetEntity()).then(invocation -> age1);

        AgRelationship r2 = mock(AgRelationship.class);
        when(age0.getRelationship("r2")).thenReturn(r2);
        when(r2.getName()).thenReturn("r2");
        when(r2.getTargetEntity()).then(invocation -> age3);

        age1 = mock(AgEntity.class);
        when(age1.getName()).thenReturn("E2");
        when(age1.getType()).thenReturn(E2.class);

        AgRelationship r11 = mock(AgRelationship.class);
        when(age1.getRelationship("r11")).thenReturn(r11);
        when(r11.getName()).thenReturn("r11");
        when(r11.getTargetEntity()).then(invocation -> age2);

        age2 = mock(AgEntity.class);
        when(age2.getName()).thenReturn("E3");
        when(age2.getType()).thenReturn(E3.class);

        age3 = mock(AgEntity.class);
        when(age3.getName()).thenReturn("E4");
        when(age3.getType()).thenReturn(E4.class);

        age4 = mock(AgEntity.class);
        when(age4.getName()).thenReturn("E5");
        when(age4.getType()).thenReturn(E5.class);

        List<EntityConstraint> r = Collections.emptyList();
        List<EntityConstraint> w = Collections.emptyList();
        this.constraintHandler = new ConstraintsHandler(r, w);
    }

    @Test
    public void testApply_FetchOffset() {

        SizeConstraints s1 = new SizeConstraints().fetchOffset(5);
        SizeConstraints s2 = new SizeConstraints().fetchOffset(0);

        ResourceEntity<E1> t1 = new RootResourceEntity<>(age0, null);
        t1.setFetchOffset(0);
        constraintHandler.constrainResponse(t1, s1, null);
        assertEquals(0, t1.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<E1> t2 = new RootResourceEntity<>(age0, null);
        t2.setFetchOffset(3);
        constraintHandler.constrainResponse(t2, s1, null);
        assertEquals(3, t2.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<E1> t3 = new RootResourceEntity<>(age0, null);
        t3.setFetchOffset(6);
        constraintHandler.constrainResponse(t3, s1, null);
        assertEquals(5, t3.getFetchOffset());
        assertEquals(5, s1.getFetchOffset());

        ResourceEntity<E1> t4 = new RootResourceEntity<>(age0, null);
        t4.setFetchOffset(6);
        constraintHandler.constrainResponse(t4, s2, null);
        assertEquals(6, t4.getFetchOffset());
        assertEquals(0, s2.getFetchOffset());
    }

    @Test
    public void testApply_FetchLimit() {

        SizeConstraints s1 = new SizeConstraints().fetchLimit(5);
        SizeConstraints s2 = new SizeConstraints().fetchLimit(0);

        ResourceEntity<E1> t1 = new RootResourceEntity<>(age0, null);
        constraintHandler.constrainResponse(t1, s1, null);
        assertEquals(5, t1.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<E1> t1_1 = new RootResourceEntity<>(age0, null);
        t1_1.setFetchLimit(0);
        constraintHandler.constrainResponse(t1_1, s1, null);
        assertEquals(5, t1_1.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<E1> t1_2 = new RootResourceEntity<>(age0, null);
        t1_2.setFetchLimit(-1);
        constraintHandler.constrainResponse(t1_2, s1, null);
        assertEquals(5, t1_2.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<E1> t2 = new RootResourceEntity<>(age0, null);
        t2.setFetchLimit(3);
        constraintHandler.constrainResponse(t2, s1, null);
        assertEquals(3, t2.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<E1> t3 = new RootResourceEntity<>(age0, null);
        t3.setFetchLimit(6);
        constraintHandler.constrainResponse(t3, s1, null);
        assertEquals(5, t3.getFetchLimit());
        assertEquals(5, s1.getFetchLimit());

        ResourceEntity<E1> t4 = new RootResourceEntity<>(age0, null);
        t4.setFetchLimit(6);
        constraintHandler.constrainResponse(t4, s2, null);
        assertEquals(6, t4.getFetchLimit());
        assertEquals(0, s2.getFetchLimit());
    }

    @Test
    public void testApply_ResourceEntity_NoTargetRel() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).attributes("a", "b");

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        appendAttribute(te1, "c");
        appendAttribute(te1, "b");

        ChildResourceEntity<?> te11 = new ChildResourceEntity<>(age2, null, null);
        appendAttribute(te11, "a1");
        appendAttribute(te11, "b1");
        te1.getChildren().put("d", te11);

        constraintHandler.constrainResponse(te1, null, tc1);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("b"));
        assertTrue(te1.getChildren().isEmpty());
    }

    @Test
    public void testApply_ResourceEntity_TargetRel() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).attributes("a", "b")
                .path("r1", Constraint.excludeAll(E2.class).attributes("n", "m"))
                .path("r1.r11", Constraint.excludeAll(E3.class).attributes("p", "r"))
                .path("r2", Constraint.excludeAll(E4.class).attributes("k", "l"));

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        appendAttribute(te1, "c");
        appendAttribute(te1, "b");

        ChildResourceEntity<?> te11 = new ChildResourceEntity<>(age1, null, null);
        appendAttribute(te11, "m");
        appendAttribute(te11, "z");
        te1.getChildren().put("r1", te11);

        ChildResourceEntity<?> te21 = new ChildResourceEntity<>(age4, null, null);
        appendAttribute(te21, "p");
        appendAttribute(te21, "z");
        te1.getChildren().put("r3", te21);

        constraintHandler.constrainResponse(te1, null, tc1);
        assertEquals(1, te1.getAttributes().size());
        assertTrue(te1.getAttributes().containsKey("b"));
        assertEquals(1, te1.getChildren().size());

        ResourceEntity<?> mergedTe11 = te1.getChildren().get("r1");
        assertNotNull(mergedTe11);
        assertTrue(mergedTe11.getChildren().isEmpty());
        assertEquals(1, mergedTe11.getAttributes().size());
        assertTrue(mergedTe11.getAttributes().containsKey("m"));
    }

    @Test
    public void testMerge_ResourceEntity_Id() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).excludeId();
        Constraint<E1> tc2 = Constraint.excludeAll(E1.class).includeId();

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        te1.includeId();
        constraintHandler.constrainResponse(te1, null, tc1);
        assertFalse(te1.isIdIncluded());

        ResourceEntity<E1> te2 = new RootResourceEntity<>(age0, null);
        te2.includeId();
        constraintHandler.constrainResponse(te2, null, tc2);
        assertTrue(te2.isIdIncluded());

        ResourceEntity<E1> te3 = new RootResourceEntity<>(age0, null);
        te3.excludeId();
        constraintHandler.constrainResponse(te3, null, tc2);
        assertFalse(te3.isIdIncluded());
    }

    @Test
    public void testMerge_CayenneExp() {

        Expression q1 = exp("a = 5");

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).and(q1);

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        constraintHandler.constrainResponse(te1, null, tc1);
        assertEquals(exp("a = 5"), te1.getQualifier());

        ResourceEntity<E1> te2 = new RootResourceEntity<>(age0, null);
        te2.andQualifier(exp("b = 'd'"));
        constraintHandler.constrainResponse(te2, null, tc1);
        assertEquals(exp("b = 'd' and a = 5"), te2.getQualifier());
    }

    @Test
    public void testMerge_MapBy() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).path("r1",
                Constraint.excludeAll(E2.class).attribute("a"));

        ChildResourceEntity<E1> te1MapByTarget = new ChildResourceEntity<>(age0, null, null);
        appendAttribute(te1MapByTarget, "b");

        ResourceEntity<E2> te1MapBy = new RootResourceEntity<>(age1, null);
        te1MapBy.getChildren().put("r1", te1MapByTarget);

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        te1.mapBy(te1MapBy, "r1.b");

        constraintHandler.constrainResponse(te1, null, tc1);
        assertNull(te1.getMapBy());
        assertNull(te1.getMapByPath());

        ChildResourceEntity<E2> te2MapByTarget = new ChildResourceEntity<>(age1, null, null);
        appendAttribute(te2MapByTarget, "a");

        ResourceEntity<E1> te2MapBy = new RootResourceEntity<>(age0, null);
        te1MapBy.getChildren().put("r1", te2MapByTarget);

        ResourceEntity<E1> te2 = new RootResourceEntity<>(age0, null);
        te2.mapBy(te2MapBy, "r1.a");

        constraintHandler.constrainResponse(te2, null, tc1);
        assertSame(te2MapBy, te2.getMapBy());
        assertEquals("r1.a", te2.getMapByPath());
    }

    @Test
    public void testMerge_MapById_Exclude() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).path("r1", Constraint.excludeAll(E2.class).excludeId());

        ChildResourceEntity<E1> te1MapByTarget = new ChildResourceEntity<>(age0, null, null);
        te1MapByTarget.includeId();

        ResourceEntity<E2> te1MapBy = new RootResourceEntity<>(age1, null);
        te1MapBy.getChildren().put("r1", te1MapByTarget);

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        te1.mapBy(te1MapBy, "r1");

        constraintHandler.constrainResponse(te1, null, tc1);
        assertNull(te1.getMapBy());
        assertNull(te1.getMapByPath());

    }

    @Test
    public void testMerge_MapById_Include() {

        Constraint<E1> tc1 = Constraint.excludeAll(E1.class).path("r1", Constraint.excludeAll(E2.class).includeId());

        ChildResourceEntity<E2> te1MapByTarget = new ChildResourceEntity<>(age1, null, null);
        te1MapByTarget.includeId();

        ResourceEntity<E1> te1MapBy = new RootResourceEntity<>(age0, null);
        te1MapBy.getChildren().put("r1", te1MapByTarget);

        ResourceEntity<E1> te1 = new RootResourceEntity<>(age0, null);
        te1.mapBy(te1MapBy, "r1");

        constraintHandler.constrainResponse(te1, null, tc1);
        assertSame(te1MapBy, te1.getMapBy());
        assertEquals("r1", te1.getMapByPath());
    }

    protected void appendAttribute(ResourceEntity<?> entity, String name) {
        entity.getAttributes().put(name, new DefaultAgAttribute(name, String.class, BeanPropertyReader.reader()));
    }
}
