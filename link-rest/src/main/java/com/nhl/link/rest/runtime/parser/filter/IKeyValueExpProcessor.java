package com.nhl.link.rest.runtime.parser.filter;

import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.meta.LrEntity;

/**
 * @since 1.17
 */
public interface IKeyValueExpProcessor {

	Expression process(LrEntity<?> entity, String queryProperty, String value);
}
