package com.nhl.link.rest.runtime.listener;

import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.*;

/**
 * @deprecated since 2.7
 */
public class ListenerInvocationTest {

	private static MethodHandle VOID_NO_ARGS;
	private static MethodHandle OBJECT_ONE_ARG;
	private static MethodHandle OBJECT_TWO_ARGS;

	private Listener mockListener;
	private ProcessingContext<Listener> mockContext;
	private BaseLinearProcessingStage<ProcessingContext<Listener>, Listener> mockStage;

	@BeforeClass
	public static void beforeClass() throws IllegalAccessException, NoSuchMethodException, SecurityException {
		MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
		VOID_NO_ARGS = LOOKUP.unreflect(Listener.class.getMethod("voidNoArg"));
		OBJECT_ONE_ARG = LOOKUP.unreflect(Listener.class.getMethod("objectOneArg", ProcessingContext.class));
		OBJECT_TWO_ARGS = LOOKUP
				.unreflect(Listener.class.getMethod("objectTwoArgs", ProcessingContext.class, BaseLinearProcessingStage.class));
	}

	@SuppressWarnings("unchecked")
	@Before
	public void before() {
		this.mockListener = mock(Listener.class);
		this.mockContext = mock(ProcessingContext.class);
		this.mockStage = mock(BaseLinearProcessingStage.class);
	}

	@Test
	public void testInvokeOld_VoidNoArg() {

		ListenerInvocation invocation = new ListenerInvocation(VOID_NO_ARGS.bindTo(mockListener), true) {

			@Override
			protected <C extends ProcessingContext<T>, T> ProcessingStage<C, T> doInvokeOld(C context,
					ProcessingStage<C, ? super T> next) throws Throwable {
				return (ProcessingStage<C, T>) methodHandle.invoke();
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invokeOld(mockContext, mockStage);
		verify(mockListener).voidNoArg();
		verifyNoMoreInteractions(mockListener);
		assertSame(mockStage, result);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testInvokeOld_ObjectOneArg() {

		BaseLinearProcessingStage<ProcessingContext<Listener>, Listener> altStage = mock(BaseLinearProcessingStage.class);
		when(mockListener.objectOneArg(mockContext)).thenReturn(altStage);

		ListenerInvocation invocation = new ListenerInvocation(OBJECT_ONE_ARG.bindTo(mockListener), false) {

			@Override
			protected <C extends ProcessingContext<T>, T> ProcessingStage<C, T> doInvokeOld(C context,
					ProcessingStage<C, ? super T> next) throws Throwable {
				return (ProcessingStage<C, T>) methodHandle.invoke(context);
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invokeOld(mockContext, mockStage);
		verify(mockListener).objectOneArg(mockContext);
		verifyNoMoreInteractions(mockListener);
		assertSame(altStage, result);
	}

	@Test
	public void testInvokeOld_ObjectTwoArgs() {

		ListenerInvocation invocation = new ListenerInvocation(OBJECT_TWO_ARGS.bindTo(mockListener), false) {

			@Override
			protected <C extends ProcessingContext<T>, T> ProcessingStage<C, T> doInvokeOld(C context,
					ProcessingStage<C, ? super T> next) throws Throwable {
				return (ProcessingStage<C, T>) methodHandle.invoke(context, next);
			}
		};

		verifyZeroInteractions(mockListener);
		Object result = invocation.invokeOld(mockContext, mockStage);
		verify(mockListener).objectTwoArgs(mockContext, mockStage);
		verifyNoMoreInteractions(mockListener);
		assertNull(result);
	}

	public static interface Listener {
		void voidNoArg();

		Object objectOneArg(ProcessingContext<?> context);

		Object objectTwoArgs(ProcessingContext<?> context, BaseLinearProcessingStage<?, ?> stage);
	}
}
