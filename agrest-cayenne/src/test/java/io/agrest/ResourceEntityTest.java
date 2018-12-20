package io.agrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import io.agrest.it.fixture.cayenne.E2;
import io.agrest.meta.AgPersistentEntity;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.Test;

public class ResourceEntityTest {

	@Test
	public void testQualifier() {
		@SuppressWarnings("unchecked")
		ResourceEntity<E2, Expression> resourceEntity = new ResourceEntity<>(mock(AgPersistentEntity.class));
		assertNull(resourceEntity.getQualifier());

		resourceEntity.andQualifier(ExpressionFactory.exp("a = 1"));
		assertEquals("a = 1", resourceEntity.getQualifier().toString());

		resourceEntity.andQualifier(ExpressionFactory.exp("b = 2"));

		if (!resourceEntity.isQualified()) {
			resourceEntity.qualify(
					(Expression e1, Expression e2) -> e1.andExp(e2),
					(Expression e1, Expression e2) -> e1.orExp(e2));
		}

		assertEquals("(a = 1) and (b = 2)", resourceEntity.getQualifier().toString());
	}
}
