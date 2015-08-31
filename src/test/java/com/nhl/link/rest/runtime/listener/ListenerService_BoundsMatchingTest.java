package com.nhl.link.rest.runtime.listener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.annotation.Fetched;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
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

	private ProcessingContext<Listener> mockContext;
	private ProcessingStage<ProcessingContext<Listener>, Listener> mockStage;
	private Listener mockListener;

	private ListenerService service;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		this.service = new ListenerService();
		this.mockListener = mock(Listener.class);
		this.mockContext = mock(ProcessingContext.class);
		this.mockStage = mock(ProcessingStage.class);
	}

	@Test
	public void testGetListenerInvocationFactories() {

		List<ListenerInvocationFactory> factories = service
				.getListenerInvocationFactories(Listener.class, EventGroup.select).get(Fetched.class);

		assertEquals(3, factories.size());
	}

}
