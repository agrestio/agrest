package com.nhl.link.rest.runtime.listener;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ProcessingContext;
import com.nhl.link.rest.processor.ProcessingStage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @since 1.19
 */
class ListenerInvocationFactoryCompiler {

	private static final Logger LOGGER = LoggerFactory.getLogger(ListenerInvocationFactoryCompiler.class);

	private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

	Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> compileFactories(Class<?> listenerType,
			ProcessingContext<?> context, EventGroup eventGroup) {

		// TODO: see todo in 'factoriesKey': we are ignoring context's own type
		// here...

		Class<?> entityType = context.getType();

		Map<Class<? extends Annotation>, List<ListenerInvocationFactory>> map = new ConcurrentHashMap<>();

		for (MethodDescriptor descriptor : collectMethods(listenerType, new HashSet<MethodDescriptor>())) {

			// will reuse 'invocationFactory' if the method has multiple
			// annotations...

			ListenerInvocationFactory invocationFactory = null;

			Method m = descriptor.getMethod();
			for (final Class<? extends Annotation> at : eventGroup.getEventsFired()) {
				Annotation a = m.getAnnotation(at);
				if (a != null) {

					if (!checkMethodIsPublic(a, m)) {
						continue;
					}

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

	private Set<MethodDescriptor> collectMethods(Class<?> listenerType, Set<MethodDescriptor> acc) {
		if (listenerType.getSuperclass() != null) {
			collectMethods(listenerType.getSuperclass(), acc);
		}
		for (Method m : listenerType.getDeclaredMethods()) {
			MethodDescriptor descriptor = new MethodDescriptor(m);
			if (acc.contains(descriptor)) {
				acc.remove(descriptor); // remove overriden method
			}
			acc.add(descriptor);
		}
		return acc;
	}

	private class MethodDescriptor {

		private final Method m;
		private final String name;
		private final Class<?>[] parameterTypes;

		MethodDescriptor(Method m) {
			this.m = m;
			name = m.getName();
			Class<?>[] parameterTypes = m.getParameterTypes();
			this.parameterTypes = Arrays.copyOf(parameterTypes, parameterTypes.length);
		}

		Method getMethod() {
			return m;
		}

		String getName() {
			return name;
		}

		Class<?>[] getParameterTypes() {
			return parameterTypes;
		}

		@Override
		public int hashCode() {
			int hash = 0;
			for (Class<?> parameterType : parameterTypes) {
				hash += parameterType.hashCode();
				hash *= 31;
			}
			hash += name.hashCode();
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof MethodDescriptor)) {
				return false;
			}
			MethodDescriptor that = (MethodDescriptor) obj;
			return this.name.equals(that.getName()) && Arrays.deepEquals(this.parameterTypes, that.getParameterTypes());
		}

		@Override
		public String toString() {
			return m.getDeclaringClass().getName() + "." + m.getName() + "(...)";
		}
	}

	private boolean checkMethodIsPublic(Annotation at, Method m) {
		boolean isPublic = Modifier.isPublic(m.getModifiers());
		if (!isPublic) {
			LOGGER.warn(String.format("Listener method %s.%s(...) for the @%s event is not public, skipping...",
					m.getDeclaringClass().getName(), m.getName(), at.annotationType().getSimpleName()));
		}
		return isPublic;
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
			checkParamType(m.getName(), paramTypes[1], ProcessingStage.class);
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
