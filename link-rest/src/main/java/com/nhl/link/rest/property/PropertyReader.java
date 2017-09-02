package com.nhl.link.rest.property;

import java.util.function.Function;

public interface PropertyReader {

	static PropertyReader forValueProducer(Function<?, ?> valueProducer) {
		// lose generics ... PropertyReader is not parameterized
		Function f = valueProducer;
		return (o, n) -> f.apply(o);
	}

	Object value(Object root, String name);
}
