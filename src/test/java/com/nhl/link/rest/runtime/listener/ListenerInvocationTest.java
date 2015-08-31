package com.nhl.link.rest.runtime.listener;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

public class ListenerInvocationTest {

	private static MethodHandle VOID_NO_ARGS;
	private static MethodHandle OBJECT_ONE_ARG;
	private static MethodHandle OBJECT_TWO_ARGS;

	private Listener mockListener;
	private ProcessingContext<Listener> mockContext;
	private ProcessingStage<ProcessingContext<Listener>, Listener> mockStage;

	@BeforeClass
	public static void beforeClass() throws IllegalAccessException, NoSuchMethodException, SecurityException {
		MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
		VOID_NO_ARGS = LOOKUP.unreflect(Listener.class.getMethod("voidNoArg"));
		OBJECT_ONE_ARG = LOOKUP.unreflect(Listener.class.getMethod("objectOneArg", ProcessingContext.class));
		OBJECT_TWO_ARGS = LOOKUP
				.unreflect(Listener.class.getMethod("objectTwoArgs", ProcessingContext.class, ProcessingStage.class));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		this.mockListener = mock(Listener.class);
		this.mockContext = mock(ProcessingContext.class);
		this.mockStage = mock(ProcessingStage.class);
	}

	@Test
	public void testInvoke_VoidNoArg() {

		ListenerInvocation invocation = new ListenerInvocation(VOID_NO_ARGS.bindTo(mockListener), true) {

			@Override
			protected <C extends ProcessingContext<T>, T> Processor<C, T> doInvoke(C context,
					Processor<C, ? super T> next) throws Throwable {
				return (Processor<C, T>) methodHandle.invoke();
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invoke(mockContext, mockStage);
		verify(mockListener).voidNoArg();
		verifyNoMoreInteractions(mockListener);
		assertSame(mockStage, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInvoke_ObjectOneArg() {

		ProcessingStage<ProcessingContext<Listener>, Listener> altStage = mock(ProcessingStage.class);
		when(mockListener.objectOneArg(mockContext)).thenReturn(altStage);

		ListenerInvocation invocation = new ListenerInvocation(OBJECT_ONE_ARG.bindTo(mockListener), false) {

			@Override
			protected <C extends ProcessingContext<T>, T> Processor<C, T> doInvoke(C context,
					Processor<C, ? super T> next) throws Throwable {
				return (Processor<C, T>) methodHandle.invoke(context);
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invoke(mockContext, mockStage);
		verify(mockListener).objectOneArg(mockContext);
		verifyNoMoreInteractions(mockListener);
		assertSame(altStage, result);
	}

	@Test
	public void testInvoke_ObjectTwoArgs() {

		ListenerInvocation invocation = new ListenerInvocation(OBJECT_TWO_ARGS.bindTo(mockListener), false) {

			@Override
			protected <C extends ProcessingContext<T>, T> Processor<C, T> doInvoke(C context,
					Processor<C, ? super T> next) throws Throwable {
				return (Processor<C, T>) methodHandle.invoke(context, next);
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invoke(mockContext, mockStage);
		verify(mockListener).objectTwoArgs(mockContext, mockStage);
		verifyNoMoreInteractions(mockListener);
		assertNull(result);
	}

	public static interface Listener {
		void voidNoArg();

		Object objectOneArg(ProcessingContext<?> context);

		Object objectTwoArgs(ProcessingContext<?> context, ProcessingStage<?, ?> stage);
	}
}
