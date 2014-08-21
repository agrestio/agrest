package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.ObjectContext;

import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.4
 */
public class CayenneUpdateResponse<T> extends UpdateResponse<T> {

	private ObjectContext updateContext;

	public CayenneUpdateResponse(Class<T> type, ObjectContext context) {
		super(type);
		this.updateContext = context;
	}

	public ObjectContext getUpdateContext() {
		return updateContext;
	}
}
