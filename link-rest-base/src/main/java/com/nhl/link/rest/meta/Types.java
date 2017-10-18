package com.nhl.link.rest.meta;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Optional;

public class Types {

    public static Optional<Type> unwrapTypeArgument(Type genericType) {
		if (!(genericType instanceof ParameterizedType)) {
			return Optional.empty();
		}

		Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
		if (typeArgs.length != 1) {
			return Optional.empty();
		}

		return Optional.of(typeArgs[0]);
	}

	public static Optional<Class<?>> getClassForTypeArgument(Type genericType) {
		return Types.unwrapTypeArgument(genericType).map(Types::getClassForType).orElse(Optional.empty());
	}

	public static Optional<Class<?>> getClassForType(Type type) {
		Class<?> ret = null;
		// the algorithm below is not universal. It doesn't check multiple bounds...
		if (type instanceof Class) {
			ret = (Class<?>) type;
		} else if (type instanceof WildcardType) {
			Type[] upperBounds = ((WildcardType) type).getUpperBounds();
			if (upperBounds.length == 1) {
				if (upperBounds[0] instanceof Class) {
					ret = (Class<?>) upperBounds[0];
				}
			}
		}
		return Optional.ofNullable(ret);
	}

	public static boolean isVoid(Class<?> type) {
		return Void.class.equals(type) || void.class.equals(type);
	}
}
