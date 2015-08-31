package com.nhl.link.rest.runtime.listener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.annotation.Fetched;
import com.nhl.link.rest.runtime.processor.select.SelectContext;

public class ListenerService_BoundsMatchingTest {

	public static class E1 {

	}

	public static class E2 extends E1 {

	}

	public static interface Listener {

		@Fetched
		void fetchedSelectContextAny(SelectContext<?> c);

		@Fetched
		void fetchedSelectContextE1(SelectContext<E1> c);

		@Fetched
		void fetchedSelectContextE2(SelectContext<E2> c);
	}

	private ListenerService service;

	@Before
	public void before() {
		this.service = new ListenerService();
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGetListenerInvocationFactories_ObjectEntity() {

		SelectContext<Object> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(Object.class);

		List<ListenerInvocationFactory> factories = service
				.getListenerInvocationFactories(Listener.class, mockContext, EventGroup.select).get(Fetched.class);

		assertEquals(1, factories.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetListenerInvocationFactories_E1Entity() {

		SelectContext<E1> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(E1.class);

		List<ListenerInvocationFactory> factories = service
				.getListenerInvocationFactories(Listener.class, mockContext, EventGroup.select).get(Fetched.class);

		assertEquals(2, factories.size());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetListenerInvocationFactories_E2Entity() {

		SelectContext<E2> mockContext = mock(SelectContext.class);
		when(mockContext.getType()).thenReturn(E2.class);

		List<ListenerInvocationFactory> factories = service
				.getListenerInvocationFactories(Listener.class, mockContext, EventGroup.select).get(Fetched.class);

		assertEquals(3, factories.size());
	}

}
