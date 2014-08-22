package com.nhl.link.rest.runtime.cayenne;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityUpdate;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.UpdateResponse;

/**
 * @since 1.4
 */
class ByKeyAndParentResponseObjectMapper<T> extends ByIdResponseObjectMapper<T> {

	private String property;
	private Expression parentQualifier;

	public ByKeyAndParentResponseObjectMapper(UpdateResponse<T> response, ObjectContext context, String property) {
		super(response, context);

		this.property = property;

		if (response.getParent() == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No parent specified");
		}

		// precompile parent qualifier - it is the same for all updates within
		// the response

		this.parentQualifier = response.getParent().qualifier(context.getEntityResolver());
	}

	@Override
	public boolean isIdempotent(EntityUpdate u) {
		return u.getValues().get(property) != null;
	}

	@Override
	public T find(EntityUpdate u) {
		Object key = u.getValues().get(property);

		// should we consider NULL as a valid unique value?
		if (key == null) {
			return null;
		}

		return findByKeyAndParent(entity, key);
	}

	protected T findByKeyAndParent(ObjEntity entity, Object key) {

		Expression qualifier = ExpressionFactory.matchExp(property, key).andExp(parentQualifier);

		SelectQuery<T> select = SelectQuery.query(response.getType());
		select.andQualifier(qualifier);
		return context.selectOne(select);
	}
}
