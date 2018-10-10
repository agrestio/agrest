package io.agrest.meta;

import io.agrest.AgException;

import javax.ws.rs.core.Response.Status;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Optional;

/**
 * @since 2.11
 */
public class Types {

	/**
	 * @return Generic type argument, if it's present.
	 *         Returns {@link Optional#empty()}, if there is more than one generic type argument.
     * @since 2.11
	 */
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

	/**
	 * @return Generic type argument, if it's present and is an instance of a Java class.
	 *         Returns {@link Optional#empty()}, if there is more than one generic type argument
	 *         or the type argument is not an instance of Java class.
	 * @since 2.11
     */
	public static Optional<Class<?>> getClassForTypeArgument(Type genericType) {
		return Types.unwrapTypeArgument(genericType).map(Types::getClassForType).orElse(Optional.empty());
	}

	/**
	 * @return Best guess, what is the most appropriate Java class representation for a given type.
	 * @since 2.11
     */
	public static Optional<Class<?>> getClassForType(Type type) {
		Class<?> ret = null;
		// the algorithm below is not universal. It doesn't check multiple bounds...
		if (type instanceof Class) {
			ret = (Class<?>) type;
		} else if (type instanceof ParameterizedType) {
			ret = (Class<?>) ((ParameterizedType) type).getRawType();
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

	/**
	 * @since 2.11
     */
	public static boolean isVoid(Class<?> type) {
		return Void.class.equals(type) || void.class.equals(type);
	}

	/**
	 * @since 2.11
     */
	public static Class<?> typeForName(String typeName) {
        if (typeName == null) {
            throw new AgException(Status.INTERNAL_SERVER_ERROR, "Type name cannot be null");
        }

        switch (typeName) {
            case "byte[]":
                return byte[].class;
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default: {
                try {
                    return Class.forName(typeName);
                } catch (ClassNotFoundException e) {
                    throw new AgException(Status.INTERNAL_SERVER_ERROR, "Unknown class: " + typeName, e);
                }
            }
        }
    }
}
