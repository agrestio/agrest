package io.agrest.jaxrs.openapi;

import com.fasterxml.jackson.databind.JavaType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * A wrapper of a {@link Type}. Needed as a common API for one of the following cases: {@link JavaType},
 * {@link ParameterizedType} or {@link Class}.
 */
public interface TypeWrapper {

    static TypeWrapper forType(Type type) {

        return switch (type) {
            case JavaType jt -> new JacksonTypeWrapper(jt);
            case ParameterizedType pt -> new ParameterizedTypeWrapper(pt);
            case Class<?> c -> new ClassTypeWrapper(c);
            case null, default -> null;
        };
    }

    Type getType();

    Class<?> getRawClass();

    TypeWrapper containedType(int index);

    int containedTypeCount();

    class JacksonTypeWrapper implements TypeWrapper {

        private final JavaType type;

        public JacksonTypeWrapper(JavaType type) {
            this.type = type;
        }

        @Override
        public JavaType getType() {
            return type;
        }

        @Override
        public Class<?> getRawClass() {
            return type.getRawClass();
        }

        @Override
        public int containedTypeCount() {
            return type.containedTypeCount();
        }

        @Override
        public TypeWrapper containedType(int index) {
            return TypeWrapper.forType(type.containedType(index));
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    class ParameterizedTypeWrapper implements TypeWrapper {

        private final ParameterizedType type;

        public ParameterizedTypeWrapper(ParameterizedType type) {
            this.type = type;
        }

        @Override
        public ParameterizedType getType() {
            return type;
        }

        @Override
        public Class<?> getRawClass() {
            return (Class<?>) type.getRawType();
        }

        @Override
        public TypeWrapper containedType(int index) {
            return TypeWrapper.forType(type.getActualTypeArguments()[index]);
        }

        @Override
        public int containedTypeCount() {
            return type.getActualTypeArguments().length;
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

    class ClassTypeWrapper implements TypeWrapper {

        private final Class<?> type;

        public ClassTypeWrapper(Class<?> type) {
            this.type = type;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public Class<?> getRawClass() {
            return type;
        }

        @Override
        public int containedTypeCount() {
            return 0;
        }

        @Override
        public TypeWrapper containedType(int index) {
            throw new UnsupportedOperationException("Class has no contained types");
        }

        @Override
        public String toString() {
            return type.toString();
        }
    }

}
