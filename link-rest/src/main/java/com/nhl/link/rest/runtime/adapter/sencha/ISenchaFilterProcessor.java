package com.nhl.link.rest.runtime.adapter.sencha;

import org.apache.cayenne.exp.Expression;

import com.nhl.link.rest.meta.LrEntity;

public interface ISenchaFilterProcessor {

	Expression process(LrEntity<?> entity, String filtersJson);
}
