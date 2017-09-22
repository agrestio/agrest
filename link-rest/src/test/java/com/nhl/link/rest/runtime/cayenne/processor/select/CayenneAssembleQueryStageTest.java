package com.nhl.link.rest.runtime.cayenne.processor.select;

import com.nhl.link.rest.ResourceEntity;
import com.nhl.link.rest.it.fixture.cayenne.E1;
import com.nhl.link.rest.it.fixture.cayenne.E2;
import com.nhl.link.rest.it.fixture.cayenne.E3;
import com.nhl.link.rest.meta.LrPersistentEntity;
import com.nhl.link.rest.meta.LrRelationship;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import com.nhl.link.rest.unit.TestWithCayenneMapping;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.query.ObjectSelect;
import org.apache.cayenne.query.Ordering;
import org.apache.cayenne.query.PrefetchTreeNode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class CayenneAssembleQueryStageTest extends TestWithCayenneMapping {

	private CayenneAssembleQueryStage makeQueryStage;

	@Before
	public void before() {
		this.makeQueryStage = new CayenneAssembleQueryStage(mockCayennePersister);
	}

	@Test
	public void testBuildQuery_Ordering() {

		Ordering o1 = E1.NAME.asc();
		Ordering o2 = E1.NAME.desc();

		ObjectSelect<E1> query = ObjectSelect.query(E1.class);
		query.orderBy(o1);

		ResourceEntity<E1> resourceEntity = getResourceEntity(E1.class);
		resourceEntity.getOrderings().add(o2);

		SelectContext<E1> context = new SelectContext<>(E1.class);
		context.setSelect(query);
		context.setEntity(resourceEntity);

		ObjectSelect<E1> amended = (ObjectSelect<E1>) makeQueryStage.buildQuery(context);
		assertSame(query, amended);
		assertEquals(2, amended.getOrderings().size());
		assertTrue(amended.getOrderings().containsAll(Arrays.asList(o1, o2)));
	}

	@Test
	public void testBuildQuery_Prefetches() {
		ObjectSelect<E2> query = ObjectSelect.query(E2.class);

		ResourceEntity<E2> resultFilter = getResourceEntity(E2.class);
		LrRelationship incoming = resultFilter.getLrEntity().getRelationship(E2.E3S.getName());
		@SuppressWarnings("unchecked")
		LrPersistentEntity<E3> target = Mockito.mock(LrPersistentEntity.class);
		resultFilter.getChildren().put(E2.E3S.getName(), new ResourceEntity<E3>(target, incoming));

		SelectContext<E2> context = new SelectContext<>(E2.class);
		context.setEntity(resultFilter);
		context.setSelect(query);

		ObjectSelect<E2> amended = (ObjectSelect<E2>) makeQueryStage.buildQuery(context);
		assertSame(query, amended);
		PrefetchTreeNode rootPrefetch = amended.getPrefetches();

		assertNotNull(rootPrefetch);
		assertEquals(1, rootPrefetch.getChildren().size());

		PrefetchTreeNode child1 = rootPrefetch.getChildren().iterator().next();
		assertEquals(E2.E3S.getName(), child1.getPath());
	}

	@Test
	public void testBuildQuery_Pagination() {

		ResourceEntity<E1> resourceEntity = new ResourceEntity<>(getLrEntity(E1.class));
		resourceEntity.setFetchLimit(10);
		resourceEntity.setFetchOffset(0);

		SelectContext<E1> c = new SelectContext<E1>(E1.class);
		c.setEntity(resourceEntity);

		ObjectSelect<E1> q1 = (ObjectSelect<E1>) makeQueryStage.buildQuery(c);

		assertEquals("Pagination in the query for paginated request is expected", 10, q1.getPageSize());
		assertEquals(0, q1.getOffset());
		assertEquals(0, q1.getLimit());

		resourceEntity.setFetchLimit(0);
		resourceEntity.setFetchOffset(0);

		ObjectSelect<E1> q2 = (ObjectSelect<E1>) makeQueryStage.buildQuery(c);
		assertEquals(0, q2.getPageSize());
		assertEquals(0, q2.getOffset());
		assertEquals(0, q2.getLimit());

		resourceEntity.setFetchLimit(0);
		resourceEntity.setFetchOffset(5);

		ObjectSelect<E1> q3 = (ObjectSelect<E1>) makeQueryStage.buildQuery(c);
		assertEquals(0, q3.getPageSize());
		assertEquals(0, q3.getOffset());
		assertEquals(0, q3.getLimit());
	}

	@Test
	public void testBuildQuery_Qualifier() {
		Expression extraQualifier = E1.NAME.eq("X");
		ResourceEntity<E1> resourceEntity = getResourceEntity(E1.class);

		resourceEntity.andQualifier(extraQualifier);

		SelectContext<E1> c1 = new SelectContext<>(E1.class);
		c1.setEntity(resourceEntity);

		ObjectSelect<E1> query = (ObjectSelect<E1>) makeQueryStage.buildQuery(c1);
		assertEquals(extraQualifier, query.getWhere());

		ObjectSelect<E1> query2 = ObjectSelect.query(E1.class);
		query2.where(E1.NAME.in("a", "b"));

		SelectContext<E1> c2 = new SelectContext<>(E1.class);
		c2.setSelect(query2);
		c2.setEntity(resourceEntity);

		ObjectSelect<E1> query2Amended = (ObjectSelect<E1>) makeQueryStage.buildQuery(c2);
		assertEquals(E1.NAME.in("a", "b").andExp(E1.NAME.eq("X")), query2Amended.getWhere());
	}

	@Test
	public void testById() {

		SelectContext<E1> c = new SelectContext<>(E1.class);
		c.setId(1);
		c.setEntity(getResourceEntity(E1.class));

		ObjectSelect<E1> s1 = (ObjectSelect<E1>) makeQueryStage.basicSelect(c);
		assertNotNull(s1);
		assertSame(E1.class, s1.getEntityType());
	}

	@Test
	public void testById_WithQuery() {
		ObjectSelect<E1> select = ObjectSelect.query(E1.class);

		SelectContext<E1> c = new SelectContext<>(E1.class);
		c.setId(1);
		c.setSelect(select);
		c.setEntity(getResourceEntity(E1.class));

		ObjectSelect<E1> s2 = (ObjectSelect<E1>) makeQueryStage.basicSelect(c);
		assertNotNull(s2);
		assertNotSame(select, s2);
		assertSame(E1.class, s2.getEntityType());
	}
}
