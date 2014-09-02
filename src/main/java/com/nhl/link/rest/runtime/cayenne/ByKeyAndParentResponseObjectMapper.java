package com.nhl.link.rest.runtime.cayenne;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.4
 */
class ByKeyAndParentResponseObjectMapper<T> extends KeyValueResponseObjectMapper<T> {

	private Expression parentQualifier;

	public ByKeyAndParentResponseObjectMapper(UpdateResponse<T> response, ObjectContext context, String property) {
		super(response, context, property, true);

		if (response.getParent() == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No parent specified");
		}

		// precompile parent qualifier - it is the same for all updates within
		// the response

		this.parentQualifier = response.getParent().qualifier(context.getEntityResolver());
	}

	@Override
	protected Object keyForObject(T object) {
		return ((DataObject) object).readProperty(keyPath);
	}

	@Override
	protected Object keyForUpdate(EntityUpdate u) {
		return u.getValues().get(keyPath);
	}

	@Override
	protected List<T> findObjects(Map<Object, Collection<EntityUpdate>> updateMap) {

		SelectQuery<T> query = SelectQuery.query(type);
		query.setQualifier(ExpressionFactory.inExp(keyPath, updateMap.keySet()).andExp(parentQualifier));
		return context.select(query);
	}

}
