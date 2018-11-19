package io.agrest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;

import io.agrest.it.fixture.cayenne.E2;
import io.agrest.meta.AgPersistentEntity;
import io.agrest.backend.exp.ExpressionFactory;
import org.junit.Test;

public class ResourceEntityTest {

	@Test
	public void testQualifier() {
		@SuppressWarnings("unchecked")
		ResourceEntity<E2> e2 = new ResourceEntity<>(mock(AgPersistentEntity.class));
		assertNull(e2.getQualifier());

//TODO: Implement Expression.toString() to pass this
//		e2.andQualifier(ExpressionFactory.exp("a = 1"));
//		assertEquals("a = 1", e2.getQualifier().toString());

// TODO: Implement Expression.toString() to pass this
//		e2.andQualifier(ExpressionFactory.exp("b = 2"));
//		assertEquals("(a = 1) and (b = 2)", e2.getQualifier().toString());
	}
}
