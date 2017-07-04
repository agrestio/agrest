package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.SelectChainInitialized;
import com.nhl.link.rest.annotation.listener.SelectRequestParsed;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import org.junit.Before;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @deprecated since 2.7
 */
public class ListenerService_SelectTest {

	private ProcessingContext<Object> mockContext;
	private BaseLinearProcessingStage<ProcessingContext<Object>, Object> mockStage;
	private SelectListener mockListener;

	private ListenerService service;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		this.service = new ListenerService();
		this.mockListener = mock(SelectListener.class);
		this.mockContext = mock(ProcessingContext.class);
		when(mockContext.getType()).thenReturn(Object.class);
		this.mockStage = mock(BaseLinearProcessingStage.class);
	}

	@Test
	public void testGetFactories() {

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = service
				.getFactories(SelectListener.class, mockContext, EventGroup.select);

		assertNotNull(factories);
		assertEquals(3, factories.size());
		assertTrue(factories.containsKey(DataFetched.class));
		assertTrue(factories.containsKey(SelectRequestParsed.class));
		assertTrue(factories.containsKey(SelectChainInitialized.class));

		assertEquals(2, factories.get(DataFetched.class).size());
		assertEquals(1, factories.get(SelectRequestParsed.class).size());
		assertEquals(1, factories.get(SelectChainInitialized.class).size());

		verifyZeroInteractions(mockListener);
		ProcessingStage<ProcessingContext<Object>, ?> stage = factories.get(SelectRequestParsed.class).get(0)
				.toInvocation(mockListener).invokeOld(mockContext, mockStage);

		verify(mockListener).afterSelectRequestParsed(mockContext);
		verifyNoMoreInteractions(mockListener);
		assertNull(stage);
	}

	@Test
	public void testGetFactories_CacheMetadata() {
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = service
				.getFactories(SelectListener.class, mockContext, EventGroup.select);
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories1 = service
				.getFactories(SelectListener.class, mockContext, EventGroup.select);

		assertSame(factories, factories1);
	}

	public static interface SelectListener {

		@DataFetched
		void afterFetched();

		@SelectRequestParsed
		Object afterSelectRequestParsed(ProcessingContext<?> context);

		@DataFetched
		void afterFetched2();

		@SelectChainInitialized
		Object afterChainInitialized(ProcessingContext<?> context, BaseLinearProcessingStage<?, ?> stage);

		Object notAnnotated(ProcessingContext<?> context, BaseLinearProcessingStage<?, ?> stage);
	}

}
