package com.nhl.link.rest.sencha;

import com.nhl.link.rest.sencha.protocol.Filter;
import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.meta.LrEntity;

import java.util.List;

public interface ISenchaFilterConstructor {

	Expression process(LrEntity<?> entity, List<Filter> filters);
}
