package io.agrest.property;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class BeanPropertyReader implements PropertyReader {

    private static final PropertyReader instance = new BeanPropertyReader();

    public static PropertyReader reader() {
        return instance;
    }

    public static PropertyReader reader(String fixedPropertyName) {
        return (root, name) -> getPropertyValue(root, fixedPropertyName);
    }

    @Override
    public Object value(Object root, String name) {
        return getPropertyValue(root, name);
    }

    private static Object getPropertyValue(Object root, String name) {
        Object result = null;

        if (root != null) {
            Class  rootClass = root.getClass();
            for (Method m : rootClass.getMethods()) {
                if (m.getName().equalsIgnoreCase("get" + name)) {
                    try {
                        return m.invoke(root);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }
}
