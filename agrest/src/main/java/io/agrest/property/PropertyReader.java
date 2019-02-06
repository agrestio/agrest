package io.agrest.property;

import java.util.function.Function;

public interface PropertyReader {

    /**
     * Creates a property reader based on a function of object producing the value.
     *
     * @param valueProducer property value as a function of object.
     * @return PropertyReader wrapping provided function.
     */
    static PropertyReader forValueProducer(Function<?, ?> valueProducer) {
        // lose generics ... PropertyReader is not parameterized
        Function f = valueProducer;
        return (o, n) -> f.apply(o);
    }

    Object value(Object root, String name);
}
