package io.agrest.property;

import org.apache.cayenne.reflect.Accessor;
import org.apache.cayenne.reflect.PropertyUtils;

public class BeanPropertyReader implements PropertyReader {

    public static PropertyReader reader(String propertyName) {
        Accessor accessor = PropertyUtils.accessor(propertyName);
        return new BeanPropertyReader(accessor);
    }

    private final Accessor accessor;

    protected BeanPropertyReader(Accessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public Object value(Object object) {
        return accessor.getValue(object);
    }
}
