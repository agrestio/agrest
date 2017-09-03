package com.nhl.link.rest.meta.compiler;

import java.lang.reflect.Method;

/**
 * @since 2.10
 */
public class PropertyMethod {

    private String name;
    private Class<?> type;
    private Method getterOrSetter;

    public PropertyMethod(String name, Class<?> type, Method getterOrSetter) {
        this.name = name;
        this.type = type;
        this.getterOrSetter = getterOrSetter;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public Method getGetterOrSetter() {
        return getterOrSetter;
    }
}
