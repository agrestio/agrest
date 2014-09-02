package com.nhl.link.rest.runtime.cayenne;

import org.apache.cayenne.Cayenne;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.Persistent;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.4
 */
public class ByIdResponseObjectMapper<T> extends KeyValueResponseObjectMapper<T> {

	public ByIdResponseObjectMapper(UpdateResponse<T> response, ObjectContext context, String keyPath) {
		super(response, context, keyPath, false);
	}

	@Override
	protected Object keyForObject(T object) {
		return Cayenne.pkForObject((Persistent) object);
	}

	@Override
	protected Object keyForUpdate(EntityUpdate u) {
		return u.getId();
	}
}
