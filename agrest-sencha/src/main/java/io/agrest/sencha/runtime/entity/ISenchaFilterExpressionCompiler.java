package io.agrest.sencha.runtime.entity;

import io.agrest.base.protocol.Exp;
import io.agrest.meta.AgEntity;
import io.agrest.sencha.protocol.Filter;

import java.util.List;

public interface ISenchaFilterExpressionCompiler {

	List<Exp> process(AgEntity<?> entity, List<Filter> filters);
}
