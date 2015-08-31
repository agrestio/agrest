package com.nhl.link.rest.runtime.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.annotation.Fetched;
import com.nhl.link.rest.annotation.SelectChainInitialized;
import com.nhl.link.rest.annotation.SelectRequestParsed;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

public class ListenerServiceTest {

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

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = service
				.getListenerInvocationFactories(Listener.class, EventGroup.select);

		assertNotNull(factories);
		assertEquals(3, factories.size());
		assertTrue(factories.containsKey(Fetched.class));
		assertTrue(factories.containsKey(SelectRequestParsed.class));
		assertTrue(factories.containsKey(SelectChainInitialized.class));

		assertEquals(2, factories.get(Fetched.class).size());
		assertEquals(1, factories.get(SelectRequestParsed.class).size());
		assertEquals(1, factories.get(SelectChainInitialized.class).size());

		verifyZeroInteractions(mockListener);
		Processor<?, ?> stage = factories.get(SelectRequestParsed.class).get(0).toInvocation(mockListener)
				.invoke(mockContext, mockStage);

		verify(mockListener).afterSelectRequestParsed(mockContext);
		verifyNoMoreInteractions(mockListener);
		assertNull(stage);
	}
	
	@Test
	public void testGetListenerInvocationFactories_CacheMetadata() {
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = service
				.getListenerInvocationFactories(Listener.class, EventGroup.select);
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories1 = service
				.getListenerInvocationFactories(Listener.class, EventGroup.select);
		
		assertSame(factories, factories1);
	}

	public static interface Listener {

		@Fetched
		void afterFetched();

		@SelectRequestParsed
		Object afterSelectRequestParsed(ProcessingContext<?> context);

		@Fetched
		void afterFetched2();

		@SelectChainInitialized
		Object afterChainInitialized(ProcessingContext<?> context, ProcessingStage<?, ?> stage);

		Object notAnnotated(ProcessingContext<?> context, ProcessingStage<?, ?> stage);
	}
}
