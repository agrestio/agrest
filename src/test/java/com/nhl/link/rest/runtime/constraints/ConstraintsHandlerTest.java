package com.nhl.link.rest.runtime.constraints;

import static com.nhl.link.rest.TreeConstraints.excludeAll;
import static org.apache.cayenne.exp.ExpressionFactory.exp;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.DataMap;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.map.ObjRelationship;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.DataResponse;
import com.nhl.link.rest.EntityConstraint;
import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.SizeConstraints;
import com.nhl.link.rest.TreeConstraints;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.DefaultLrAttribute;

public class ConstraintsHandlerTest {

	private ConstraintsHandler constraintHandler;

	private LrEntity<Object> lre0;
	private LrEntity<Object> lre1;
	private LrEntity<Object> lre2;
	private LrEntity<Object> lre3;
	private LrEntity<Object> lre4;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {

		List<EntityConstraint> r = Collections.emptyList();
		List<EntityConstraint> w = Collections.emptyList();
		this.constraintHandler = new ConstraintsHandler(r, w);

		DataMap dm = new DataMap();

		ObjEntity e0 = new ObjEntity("Test");
		dm.addObjEntity(e0);

		ObjEntity e1 = new ObjEntity("Test1");
		dm.addObjEntity(e1);

		ObjEntity e2 = new ObjEntity("Test2");
		dm.addObjEntity(e2);

		ObjEntity e3 = new ObjEntity("Test3");
		dm.addObjEntity(e3);

		ObjRelationship r01 = new ObjRelationship("r1");
		r01.setTargetEntityName(e1.getName());
		e0.addRelationship(r01);

		ObjRelationship r12 = new ObjRelationship("r11");
		r12.setTargetEntityName(e2.getName());
		e1.addRelationship(r12);

		ObjRelationship r03 = new ObjRelationship("r2");
		r03.setTargetEntityName(e3.getName());
		e0.addRelationship(r03);

		lre0 = mock(LrEntity.class);
		when(lre0.getObjEntity()).thenReturn(e0);
		when(lre0.getType()).thenReturn(Object.class);

		lre1 = mock(LrEntity.class);
		when(lre1.getObjEntity()).thenReturn(e1);
		when(lre1.getType()).thenReturn(Object.class);

		lre2 = mock(LrEntity.class);
		when(lre2.getObjEntity()).thenReturn(e2);
		when(lre2.getType()).thenReturn(Object.class);

		lre3 = mock(LrEntity.class);
		when(lre3.getObjEntity()).thenReturn(e3);
		when(lre3.getType()).thenReturn(Object.class);

		lre4 = mock(LrEntity.class);
		when(lre4.getObjEntity()).thenReturn(null);
		when(lre4.getType()).thenReturn(Object.class);
	}

	@Test
	public void testApply_FetchOffset() {

		SizeConstraints s1 = new SizeConstraints().fetchOffset(5);
		SizeConstraints s2 = new SizeConstraints().fetchOffset(0);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withFetchOffset(0);
		constraintHandler.constrainResponse(t1, s1, null);
		assertEquals(0, t1.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t2 = DataResponse.forType(Object.class).withFetchOffset(3);
		constraintHandler.constrainResponse(t2, s1, null);
		assertEquals(3, t2.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t3 = DataResponse.forType(Object.class).withFetchOffset(6);
		constraintHandler.constrainResponse(t3, s1, null);
		assertEquals(5, t3.getFetchOffset());
		assertEquals(5, s1.getFetchOffset());

		DataResponse<?> t4 = DataResponse.forType(Object.class).withFetchOffset(6);
		constraintHandler.constrainResponse(t4, s2, null);
		assertEquals(6, t4.getFetchOffset());
		assertEquals(0, s2.getFetchOffset());
	}

	@Test
	public void testApply_FetchLimit() {

		SizeConstraints s1 = new SizeConstraints().fetchLimit(5);
		SizeConstraints s2 = new SizeConstraints().fetchLimit(0);

		DataResponse<?> t1 = DataResponse.forType(Object.class).withFetchLimit(0);
		constraintHandler.constrainResponse(t1, s1, null);
		assertEquals(0, t1.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t2 = DataResponse.forType(Object.class).withFetchLimit(3);
		constraintHandler.constrainResponse(t2, s1, null);
		assertEquals(3, t2.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t3 = DataResponse.forType(Object.class).withFetchLimit(6);
		constraintHandler.constrainResponse(t3, s1, null);
		assertEquals(5, t3.getFetchLimit());
		assertEquals(5, s1.getFetchLimit());

		DataResponse<?> t4 = DataResponse.forType(Object.class).withFetchLimit(6);
		constraintHandler.constrainResponse(t4, s2, null);
		assertEquals(6, t4.getFetchLimit());
		assertEquals(0, s2.getFetchLimit());
	}

	@Test
	public void testApply_ClientEntity_NoTargetRel() {

		TreeConstraints<Object> tc1 = TreeConstraints.excludeAll(Object.class).attributes("a", "b");

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		appendAttribute(te1, "c");
		appendAttribute(te1, "b");

		ResourceEntity<?> te11 = new ResourceEntity<>(lre2);
		appendAttribute(te11, "a1");
		appendAttribute(te11, "b1");
		te1.getChildren().put("d", te11);

		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);

		constraintHandler.constrainResponse(t1, null, tc1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().containsKey("b"));
		assertTrue(t1.getEntity().getChildren().isEmpty());
	}

	@Test
	public void testApply_ClientEntity_TargetRel() {

		TreeConstraints<Object> tc1 = TreeConstraints.excludeAll(Object.class).attributes("a", "b")
				.path("r1", TreeConstraints.excludeAll(Object.class).attributes("n", "m"))
				.path("r1.r11", TreeConstraints.excludeAll(Object.class).attributes("p", "r"))
				.path("r2", TreeConstraints.excludeAll(Object.class).attributes("k", "l"));

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		appendAttribute(te1, "c");
		appendAttribute(te1, "b");

		ResourceEntity<?> te11 = new ResourceEntity<>(lre1);
		appendAttribute(te11, "m");
		appendAttribute(te11, "z");
		te1.getChildren().put("r1", te11);

		ResourceEntity<?> te21 = new ResourceEntity<>(lre4);
		appendAttribute(te21, "p");
		appendAttribute(te21, "z");
		te1.getChildren().put("r3", te21);

		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);

		constraintHandler.constrainResponse(t1, null, tc1);
		assertEquals(1, t1.getEntity().getAttributes().size());
		assertTrue(t1.getEntity().getAttributes().containsKey("b"));
		assertEquals(1, t1.getEntity().getChildren().size());

		ResourceEntity<?> mergedTe11 = t1.getEntity().getChildren().get("r1");
		assertNotNull(mergedTe11);
		assertTrue(mergedTe11.getChildren().isEmpty());
		assertEquals(1, mergedTe11.getAttributes().size());
		assertTrue(mergedTe11.getAttributes().containsKey("m"));
	}

	@Test
	public void testMerge_ClientEntity_Id() {

		TreeConstraints<Object> tc1 = TreeConstraints.excludeAll(Object.class).excludeId();
		TreeConstraints<Object> tc2 = TreeConstraints.excludeAll(Object.class).includeId();

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		te1.includeId();
		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		constraintHandler.constrainResponse(t1, null, tc1);
		assertFalse(t1.getEntity().isIdIncluded());

		ResourceEntity<Object> te2 = new ResourceEntity<>(lre0);
		te2.includeId();
		DataResponse<Object> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		constraintHandler.constrainResponse(t2, null, tc2);
		assertTrue(t2.getEntity().isIdIncluded());

		ResourceEntity<Object> te3 = new ResourceEntity<>(lre0);
		te3.excludeId();
		DataResponse<Object> t3 = DataResponse.forType(Object.class).withClientEntity(te3);
		constraintHandler.constrainResponse(t3, null, tc2);
		assertFalse(t3.getEntity().isIdIncluded());
	}

	@Test
	public void testMerge_CayenneExp() {

		Expression q1 = exp("a = 5");

		TreeConstraints<Object> tc1 = TreeConstraints.excludeAll(Object.class).and(q1);

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		constraintHandler.constrainResponse(t1, null, tc1);
		assertEquals(exp("a = 5"), t1.getEntity().getQualifier());

		ResourceEntity<Object> te2 = new ResourceEntity<>(lre0);
		te2.andQualifier(exp("b = 'd'"));
		DataResponse<Object> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		constraintHandler.constrainResponse(t2, null, tc1);
		assertEquals(exp("b = 'd' and a = 5"), t2.getEntity().getQualifier());
	}

	@Test
	public void testMerge_MapBy() {

		TreeConstraints<Object> tc1 = excludeAll(Object.class).path("r1",
				TreeConstraints.excludeAll(Object.class).attribute("a"));

		ResourceEntity<Object> te1MapByTarget = new ResourceEntity<>(lre0);
		appendAttribute(te1MapByTarget, "b");

		ResourceEntity<Object> te1MapBy = new ResourceEntity<>(lre1);
		te1MapBy.getChildren().put("r1", te1MapByTarget);

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		te1.mapBy(te1MapBy, "r1.b");

		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		constraintHandler.constrainResponse(t1, null, tc1);
		assertNull(t1.getEntity().getMapBy());
		assertNull(t1.getEntity().getMapByPath());

		ResourceEntity<Object> te2MapByTarget = new ResourceEntity<>(lre1);
		appendAttribute(te2MapByTarget, "a");

		ResourceEntity<Object> te2MapBy = new ResourceEntity<>(lre0);
		te1MapBy.getChildren().put("r1", te2MapByTarget);

		ResourceEntity<Object> te2 = new ResourceEntity<>(lre0);
		te2.mapBy(te2MapBy, "r1.a");

		DataResponse<Object> t2 = DataResponse.forType(Object.class).withClientEntity(te2);
		constraintHandler.constrainResponse(t2, null, tc1);
		assertSame(te2MapBy, t2.getEntity().getMapBy());
		assertEquals("r1.a", t2.getEntity().getMapByPath());
	}

	@Test
	public void testMerge_MapById_Exclude() {

		TreeConstraints<Object> tc1 = excludeAll(Object.class).path("r1", excludeAll(Object.class).excludeId());

		ResourceEntity<Object> te1MapByTarget = new ResourceEntity<>(lre0);
		te1MapByTarget.includeId();

		ResourceEntity<Object> te1MapBy = new ResourceEntity<>(lre1);
		te1MapBy.getChildren().put("r1", te1MapByTarget);

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		te1.mapBy(te1MapBy, "r1");

		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		constraintHandler.constrainResponse(t1, null, tc1);
		assertNull(t1.getEntity().getMapBy());
		assertNull(t1.getEntity().getMapByPath());

	}

	@Test
	public void testMerge_MapById_Include() {

		TreeConstraints<Object> tc1 = excludeAll(Object.class).path("r1", excludeAll(Object.class).includeId());

		ResourceEntity<Object> te1MapByTarget = new ResourceEntity<>(lre1);
		te1MapByTarget.includeId();

		ResourceEntity<Object> te1MapBy = new ResourceEntity<>(lre0);
		te1MapBy.getChildren().put("r1", te1MapByTarget);

		ResourceEntity<Object> te1 = new ResourceEntity<>(lre0);
		te1.mapBy(te1MapBy, "r1");

		DataResponse<Object> t1 = DataResponse.forType(Object.class).withClientEntity(te1);
		constraintHandler.constrainResponse(t1, null, tc1);
		assertSame(te1MapBy, t1.getEntity().getMapBy());
		assertEquals("r1", t1.getEntity().getMapByPath());
	}

	protected void appendAttribute(ResourceEntity<?> entity, String name) {
		entity.getAttributes().put(name, new DefaultLrAttribute(name, String.class.getName()));
	}
}
