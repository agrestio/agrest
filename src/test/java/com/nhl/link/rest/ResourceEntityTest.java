package com.nhl.link.rest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.cayenne.exp.ExpressionFactory;
import org.junit.Test;

import com.nhl.link.rest.it.fixture.cayenne.E2;

public class ResourceEntityTest {

	@Test
	public void testQualifier() {
		ResourceEntity<E2> e2 = new ResourceEntity<>(E2.class);
		assertNull(e2.getQualifier());

		e2.andQualifier(ExpressionFactory.exp("a = 1"));
		assertEquals("a = 1", e2.getQualifier().toString());

		e2.andQualifier(ExpressionFactory.exp("b = 2"));
		assertEquals("(a = 1) and (b = 2)", e2.getQualifier().toString());
	}
}
