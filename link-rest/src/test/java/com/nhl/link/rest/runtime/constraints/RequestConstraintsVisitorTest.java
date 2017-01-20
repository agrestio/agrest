package com.nhl.link.rest.runtime.constraints;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.nhl.link.rest.constraints.ConstrainedLrEntity;
import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.meta.LrEntity;

public class RequestConstraintsVisitorTest {

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

		visitor.visitExcludePropertiesConstraint("c", "a1");
		assertEquals(1, visitor.getChildren().size());
		assertTrue(visitor.getChildren().containsKey("c1"));
		assertEquals(1, visitor.getAttributes().size());
		assertTrue(visitor.getAttributes().contains("a"));
	}
}
