package com.nhl.link.rest.constraints;

import com.nhl.link.rest.meta.LrEntity;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ConstrainedLrEntityTest {

	private ConstrainedLrEntity visitor;

	@Before
	public void before() {
		LrEntity<?> entity = mock(LrEntity.class);
		visitor = new ConstrainedLrEntity(entity);
	}

	@Test
	public void testVisitExcludePropertiesConstraint() {

		visitor.getChildren().put("c", mock(ConstrainedLrEntity.class));
		visitor.getChildren().put("c1", mock(ConstrainedLrEntity.class));
		visitor.getAttributes().add("a");
		visitor.getAttributes().add("a1");

		visitor.excludeProperties("c", "a1");
		assertEquals(1, visitor.getChildren().size());
		assertTrue(visitor.getChildren().containsKey("c1"));
		assertEquals(1, visitor.getAttributes().size());
		assertTrue(visitor.getAttributes().contains("a"));
	}
}
