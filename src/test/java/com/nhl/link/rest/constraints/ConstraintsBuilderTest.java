package com.nhl.link.rest.constraints;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Test;

import com.nhl.link.rest.it.fixture.cayenne.E4;

public class ConstraintsBuilderTest {

	@Test
	public void testExcludeAll() {

		ConstraintVisitor visitor = mock(ConstraintVisitor.class);
		ConstraintsBuilder<E4> tc = ConstraintsBuilder.excludeAll(E4.class);

		tc.accept(visitor);

		verifyZeroInteractions(visitor);
	}

	@Test
	public void testIdOnly() {

		ConstraintVisitor visitor = mock(ConstraintVisitor.class);
		ConstraintsBuilder<E4> tc = ConstraintsBuilder.idOnly(E4.class);

		tc.accept(visitor);

		verify(visitor).visitIncludeIdConstraint(true);
		verifyNoMoreInteractions(visitor);
	}

	@Test
	public void testIdAndAttributes() {

		ConstraintVisitor visitor = mock(ConstraintVisitor.class);
		ConstraintsBuilder<E4> tc = ConstraintsBuilder.idAndAttributes(E4.class);

		tc.accept(visitor);

		verify(visitor).visitIncludeIdConstraint(true);
		verify(visitor).visitIncludeAllAttributesConstraint();
		verifyNoMoreInteractions(visitor);
	}
}
