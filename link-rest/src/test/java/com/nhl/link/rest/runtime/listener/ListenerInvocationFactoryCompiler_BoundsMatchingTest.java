package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.runtime.processor.select.SelectContext;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @deprecated since 2.7
 */
public class ListenerInvocationFactoryCompiler_BoundsMatchingTest {

	public static class E1 {

	}

	public static class E2 extends E1 {

	}

	public static interface Listener {

		@DataFetched
		void fetchedSelectContextAny(SelectContext<?> c);

		@DataFetched
		void fetchedSelectContextE1(SelectContext<E1> c);

		@DataFetched
		void fetchedSelectContextExtendsE1(SelectContext<? extends E1> c);
		
		@DataFetched
		void fetchedSelectContextE2(SelectContext<E2> c);
	}

	private ListenerInvocationFactoryCompiler compiler;

	@Before
	public void before() {
		this.compiler = new ListenerInvocationFactoryCompiler();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCompileFactories_ObjectEntity() {

		SelectContext<Object> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(Object.class);

		List<ListenerInvocationFactory> factories = compiler
				.compileFactories(Listener.class, mockContext, EventGroup.select).get(DataFetched.class);

		assertEquals(1, factories.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCompileFactories_E1Entity() {

		SelectContext<E1> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(E1.class);

		List<ListenerInvocationFactory> factories = compiler
				.compileFactories(Listener.class, mockContext, EventGroup.select).get(DataFetched.class);

		assertEquals(3, factories.size());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCompileFactories_E2Entity() {

		SelectContext<E2> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(E2.class);

		List<ListenerInvocationFactory> factories = compiler
				.compileFactories(Listener.class, mockContext, EventGroup.select).get(DataFetched.class);

		assertEquals(4, factories.size());
	}

}
