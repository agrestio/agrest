package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;

/**
 * @since 1.19
 */
class ListenerInvocationFactoryCompiler {

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> compileFactories(Class<?> listenerType,
			ProcessingContext<?> context, EventGroup eventGroup) {

		// TODO: see todo in 'factoriesKey': we are ignoring context's own type
		// here...

		Class<?> entityType = context.getType();

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> map = new ConcurrentHashMap<>();

		for (final Method m : listenerType.getMethods()) {

			// will reuse 'invocationFactory' if the method has multiple
			// annotations...

			ListenerInvocationFactory invocationFactory = null;

			for (final Class<? extends Annotation> at : eventGroup.getEventsFired()) {
				Annotation a = m.getAnnotation(at);
				if (a != null) {

					if (invocationFactory == null) {
						invocationFactory = compileFactory(m, entityType);

						// if 'null' is returned, the method is a valid listener
						// method, but entity type doesn't match our request...
						// If that's the case, skip it

						if (invocationFactory == null) {
							break;
						}
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

	private ListenerInvocationFactory compileFactory(final Method m, Class<?> entityType) {

		final boolean voidMethod = m.getReturnType().equals(Void.TYPE);
		final Invoker invoker = checkParamTypesAndCompileInvoker(m);

		// method bounds checking ... mismatch is not a cause for an error; the
		// method simply doesn't match current context type
		if (!invoker.getEntityType().isAssignableFrom(entityType)) {
			return null;
		}

		// TODO: check non-void method return type compatibility...

		return new ListenerInvocationFactory() {

			MethodHandle handle = toHandle(m);

			@Override
			public ListenerInvocation toInvocation(Object listener) {

				MethodHandle invokable = handle.bindTo(listener);
				return new ListenerInvocation(invokable, voidMethod) {

					@SuppressWarnings("unchecked")
					@Override
					protected <C extends ProcessingContext<T>, T> ProcessingStage<C, ? super T> doInvoke(C context,
							ProcessingStage<C, ? super T> next) throws Throwable {

						return (ProcessingStage<C, ? super T>) invoker.invoke(methodHandle, context, next);
					}
				};
			}
		};
	}

	Invoker checkParamTypesAndCompileInvoker(Method m) {

		Class<?>[] paramTypes = m.getParameterTypes();
		Type[] genericParamTypes = m.getGenericParameterTypes();

		switch (paramTypes.length) {
		case 0:
			return new Invoker() {

				@Override
				public Class<Object> getEntityType() {
					// works with any type...
					return Object.class;
				}

				@Override
				public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
						ProcessingStage<C, ? super T> next) throws Throwable {
					return handle.invoke();
				}
			};

		case 1:

			checkParamType(m.getName(), paramTypes[0], ProcessingContext.class);
			final Class<?> entityType1 = entityTypeForParamType(genericParamTypes[0]);

			return new Invoker() {

				@Override
				public Class<?> getEntityType() {
					return entityType1;
				}

				@Override
				public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
						ProcessingStage<C, ? super T> next) throws Throwable {
					return handle.invoke(context);
				}
			};

		case 2:
			checkParamType(m.getName(), paramTypes[0], ProcessingContext.class);
			checkParamType(m.getName(), paramTypes[1], BaseLinearProcessingStage.class);
			final Class<?> entityType2 = entityTypeForParamType(genericParamTypes[0]);

			return new Invoker() {

				@Override
				public Class<?> getEntityType() {
					return entityType2;
				}

				@Override
				public <C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context,
						ProcessingStage<C, ? super T> next) throws Throwable {
					return handle.invoke(context, next);
				}
			};

		default:
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Annotated method is expected to have at most 2 arguments. Method '" + m.getName() + "' has "
							+ paramTypes.length);
		}
	}

	Class<?> entityTypeForParamType(Type paramType) {

		if (paramType instanceof ParameterizedType) {

			// the algorithm below is not universal. It doesn't check multiple
			// bounds...

			Type[] typeArgs = ((ParameterizedType) paramType).getActualTypeArguments();
			if (typeArgs.length == 1) {
				if (typeArgs[0] instanceof Class) {
					return (Class<?>) typeArgs[0];
				} else if (typeArgs[0] instanceof WildcardType) {
					Type[] upperBounds = ((WildcardType) typeArgs[0]).getUpperBounds();
					if (upperBounds.length == 1) {
						if (upperBounds[0] instanceof Class) {
							return (Class<?>) upperBounds[0];
						}
					}
				}
			}
		}

		return Object.class;
	}

	void checkParamType(String methodName, Class<?> type, Class<?> expectedType) {
		if (!expectedType.isAssignableFrom(type)) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unexpected parameter type for listener method '"
					+ methodName + "'. Should be " + expectedType.getName() + ", but was " + type.getName());
		}
	}

	MethodHandle toHandle(Method m) {
		try {
			return LOOKUP.unreflect(m);
		} catch (IllegalAccessException e) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
					"Can't get a MethodHandle for a method: " + m.getName(), e);
		}
	}

	private interface Invoker {

		<C extends ProcessingContext<T>, T> Object invoke(MethodHandle handle, C context, ProcessingStage<C, ? super T> next)
				throws Throwable;

		Class<?> getEntityType();
	}

}
