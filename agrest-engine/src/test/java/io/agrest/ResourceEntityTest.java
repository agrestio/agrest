package io.agrest;

import io.agrest.pojo.model.P1;
import io.agrest.meta.AgEntity;
import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ResourceEntityTest {

	@Test
	public void testQualifier() {
		@SuppressWarnings("unchecked")
		ResourceEntity<P1> p1 = new RootResourceEntity<>(mock(AgEntity.class), null);
		assertNull(p1.getQualifier());

		p1.andQualifier(ExpressionFactory.exp("a = 1"));
		assertEquals("a = 1", p1.getQualifier().toString());

		p1.andQualifier(ExpressionFactory.exp("b = 2"));
		assertEquals("(a = 1) and (b = 2)", p1.getQualifier().toString());
	}
}
