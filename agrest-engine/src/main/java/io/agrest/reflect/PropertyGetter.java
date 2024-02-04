package io.agrest.reflect;

import io.agrest.AgException;

import java.lang.reflect.Method;

/**
 * @since 2.10
 */
public class PropertyGetter {

    private final String name;
    private final Class<?> type;
    private final Method method;

    public PropertyGetter(String name, Method method) {
        this.name = name;
        this.type = method.getReturnType();
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public Object getValue(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return method.invoke(object, (Object[]) null);
        } catch (Throwable th) {
            throw AgException.internalServerError(th, "Error reading property: %s", name);
        }
    }
}
