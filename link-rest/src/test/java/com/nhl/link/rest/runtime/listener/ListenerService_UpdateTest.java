package com.nhl.link.rest.runtime.listener;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.nhl.link.rest.annotation.listener.DataFetched;
import com.nhl.link.rest.annotation.listener.DataStoreUpdated;
import com.nhl.link.rest.annotation.listener.UpdateChainInitialized;
import com.nhl.link.rest.annotation.listener.UpdateRequestParsed;
import com.nhl.link.rest.annotation.listener.UpdateResponseUpdated;
import com.nhl.link.rest.annotation.listener.UpdateServerParamsApplied;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.update.UpdateContext;

public class ListenerService_UpdateTest {

	private ProcessingContext<Object> mockContext;
	private BaseLinearProcessingStage<ProcessingContext<Object>, Object> mockStage;
	private UpdateListener mockListener;

	private ListenerService service;

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		this.service = new ListenerService();
		this.mockListener = mock(UpdateListener.class);
		this.mockContext = mock(ProcessingContext.class);
		when(mockContext.getType()).thenReturn(Object.class);
		this.mockStage = mock(BaseLinearProcessingStage.class);
	}

	@Test
	public void testGetFactories_Update() {

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factories = service
				.getFactories(UpdateListener.class, mockContext, EventGroup.update);

		assertNotNull(factories);
		assertEquals(5, factories.size());
		assertTrue(factories.containsKey(UpdateRequestParsed.class));
		assertTrue(factories.containsKey(UpdateChainInitialized.class));
		assertTrue(factories.containsKey(UpdateServerParamsApplied.class));
		assertTrue(factories.containsKey(DataStoreUpdated.class));
		assertTrue(factories.containsKey(UpdateResponseUpdated.class));

		assertEquals(2, factories.get(UpdateRequestParsed.class).size());
		assertEquals(1, factories.get(UpdateChainInitialized.class).size());
		assertEquals(1, factories.get(UpdateServerParamsApplied.class).size());
		assertEquals(1, factories.get(DataStoreUpdated.class).size());
		assertEquals(1, factories.get(UpdateResponseUpdated.class).size());

		verifyZeroInteractions(mockListener);
		ProcessingStage<ProcessingContext<Object>, ?> stage = factories.get(DataStoreUpdated.class).get(0)
				.toInvocation(mockListener).invoke(mockContext, mockStage);

		verify(mockListener).afterDataStoreUpdate(mockContext);
		verifyNoMoreInteractions(mockListener);
		assertNull(stage);
	}

	public static interface UpdateListener {

		@DataFetched
		void selectAnnotationMustBeIgnored();

		Object notAnnotatedMustBeIgnored(ProcessingContext<?> context, BaseLinearProcessingStage<?, ?> stage);

		@UpdateRequestParsed
		Object afterUpdateRequestParsed(UpdateContext<?> context);

		@UpdateRequestParsed
		Object afterUpdateRequestParsed2(ProcessingContext<?> context);

		@UpdateChainInitialized
		Object afterChainInitialized(UpdateContext<?> context, BaseLinearProcessingStage<?, ?> stage);

		@UpdateServerParamsApplied
		Object afterUpdateServerParamsApplied(ProcessingContext<?> context);

		@DataStoreUpdated
		Object afterDataStoreUpdate(ProcessingContext<?> context);

		@UpdateResponseUpdated
		Object afterUpdateResponseUpdated(ProcessingContext<?> context);
	}

}
