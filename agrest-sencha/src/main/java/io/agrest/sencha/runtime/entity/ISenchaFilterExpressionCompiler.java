package io.agrest.sencha.runtime.entity;

import io.agrest.meta.LrEntity;
import io.agrest.sencha.protocol.Filter;
import org.apache.cayenne.exp.Expression;


import java.util.List;

public interface ISenchaFilterExpressionCompiler {

	Expression process(LrEntity<?> entity, List<Filter> filters);
}
