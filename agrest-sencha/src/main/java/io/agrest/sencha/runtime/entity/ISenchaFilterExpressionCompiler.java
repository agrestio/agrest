package io.agrest.sencha.runtime.entity;

import io.agrest.meta.AgEntity;
import io.agrest.sencha.protocol.Filter;
import io.agrest.backend.exp.Expression;


import java.util.List;

public interface ISenchaFilterExpressionCompiler {

	Expression process(AgEntity<?> entity, List<Filter> filters);
}
