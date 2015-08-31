package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;

/**
 * @since 1.19
 */
public class ListenerService implements IListenerService {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	private ConcurrentMap<String, Map<Class<? extends Annotation>, List<ListenerInvocationFactory>>>[] factories;

	@SuppressWarnings("unchecked")
	public ListenerService() {
		this.factories = new ConcurrentMap[EventGroup.values().length];
		for (int i = 0; i < factories.length; i++) {
			factories[i] = new ConcurrentHashMap<>();
		}
	}

	private String factoriesKey(Class<?> listenerType, ProcessingContext<?> context) {
		// TODO: we are ignoring context's own type here, leaving it up to the
		// user to provide the right context type; may need to include that in
		// the key as well
		return listenerType.getName() + "|" + context.getType().getName();
	}

	@Override
	public Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> getListenerInvocationFactories(
			Class<?> listenerType, ProcessingContext<?> context, EventGroup eventGroup) {

		if (listenerType == null) {
			throw new NullPointerException("Null type");
		}

		if (eventGroup == null) {
			throw new NullPointerException("Null eventGroup");
		}

		ConcurrentMap<String, Map<Class<? extends Annotation>, List<ListenerInvocationFactory>>> factoriesForEventGroup = factories[eventGroup
				.ordinal()];

		String key = factoriesKey(listenerType, context);
		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> factoriesForListener = factoriesForEventGroup
				.get(key);

		if (factoriesForListener == null) {

			Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> newFactories = compileFactories(
					listenerType, context, eventGroup);
			Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> oldFactories = factoriesForEventGroup
					.putIfAbsent(key, newFactories);
			factoriesForListener = oldFactories != null ? oldFactories : newFactories;
		}

		return factoriesForListener;
	}

	Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> compileFactories(Class<?> listenerType,
			ProcessingContext<?> context, EventGroup eventGroup) {

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> map = new ConcurrentHashMap<>();

		for (final Method m : listenerType.getMethods()) {

			// will reuse 'invocationFactory' if the method has multiple
			// annotations...

			ListenerInvocationFactory invocationFactory = null;

			for (final Class<? extends Annotation> at : eventGroup.getEventsFired()) {
				Annotation a = m.getAnnotation(at);
				if (a != null) {

					if (invocationFactory == null) {
						invocationFactory = new DefaultInvocationFactory(m);
					}

					List<ListenerInvocationFactory> list = map.get(at);
					if (list == null) {
						list = new ArrayList<>();
						map.put(at, list);
					}

					list.add(invocationFactory);
				}
			}
		}

		return map;
	}

	private void checkParamType(String methodName, Class<?> type, Class<?> expectedType) {
		if (!expectedType.isAssignableFrom(type)) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unexpected parameter type for listener method '"
					+ methodName + "'. Should be " + expectedType.getName() + ", but was " + type.getName());
		}
	}

	private MethodHandle toHandle(Method m) {
		try {
			return LOOKUP.unreflect(m);
		} catch (IllegalAccessException e) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Can't get a MethodHandle for a method: " + m.getName(), e);
		}
	}

	private final class DefaultInvocationFactory implements ListenerInvocationFactory {

		private final MethodHandle handle;
		private final int argsLen;
		private final Invoker invoker;
		private final boolean voidMethod;

		DefaultInvocationFactory(Method m) {

			this.handle = toHandle(m);
			this.argsLen = handle.type().parameterCount();
			this.voidMethod = handle.type().returnType().equals(Void.TYPE);

			Class<?>[] paramTypes = handle.type().parameterArray();

			// paramTypes[0] is the listener itself... ignoring it here, we are
			// only interested in params passed to the listener

			switch (argsLen) {
			case 1:
				this.invoker = new Invoker() {

					@Override
					public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
							Processor<C, ? super T> next) throws Throwable {
						return handle.invoke();
					}
				};
				break;
			case 2:

				checkParamType(m.getName(), paramTypes[1], ProcessingContext.class);

				this.invoker = new Invoker() {

					@Override
					public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
							Processor<C, ? super T> next) throws Throwable {
						return handle.invoke(context);
					}
				};

				break;
			case 3:
				checkParamType(m.getName(), paramTypes[1], ProcessingContext.class);
				checkParamType(m.getName(), paramTypes[2], ProcessingStage.class);

				this.invoker = new Invoker() {

					@Override
					public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
							Processor<C, ? super T> next) throws Throwable {
						return handle.invoke(context, next);
					}
				};
				break;
			default:
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
						"Annotated method is expected to have at most 2 arguments. Method '" + m.getName() + "' has "
								+ argsLen);
			}
		}

		@Override
		public ListenerInvocation toInvocation(Object listener) {

			MethodHandle invokable = handle.bindTo(listener);
			return new ListenerInvocation(invokable, voidMethod) {

				@SuppressWarnings("unchecked")
				@Override
				protected <C extends ProcessingContext<T>, T> Processor<C, ? super T> doInvoke(C context,
						Processor<C, ? super T> next) throws Throwable {

					return (Processor<C, ? super T>) invoker.invoke(methodHandle, context, next);
				}
			};
		}
	}

	private interface Invoker {
		<C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context, Processor<C, ? super T> next)
				throws Throwable;
	}

}
