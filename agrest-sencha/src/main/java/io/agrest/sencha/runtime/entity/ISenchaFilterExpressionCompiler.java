package io.agrest.sencha.runtime.entity;

import io.agrest.meta.AgEntity;
import io.agrest.sencha.protocol.Filter;


import java.util.List;

public interface ISenchaFilterExpressionCompiler<E> {

	E process(AgEntity<?> entity, List<Filter> filters);
}
