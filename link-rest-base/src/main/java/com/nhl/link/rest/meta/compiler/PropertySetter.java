package com.nhl.link.rest.meta.compiler;

import com.nhl.link.rest.LinkRestException;

import javax.ws.rs.core.Response;
import java.lang.reflect.Method;

/**
 * @since 2.11
 */
public class PropertySetter {

    private String name;
    private Class<?> type;
    private Method method;

    public PropertySetter(String name, Class<?> type, Method method) {
        this.name = name;
        this.type = type;
        this.method = method;
    }

    public String getName() {
        return name;
    }

    public Class<?> getParameterType() {
        return type;
    }

    public Method getMethod() {
        return method;
    }

    public void setValue(Object object, Object value) {
        if (object == null) {
            return;
        }

        try {
            method.invoke(object, value);
        } catch (Throwable th) {
            throw new LinkRestException(Response.Status.INTERNAL_SERVER_ERROR, "Error writing property: " + name, th);
        }
    }
}
