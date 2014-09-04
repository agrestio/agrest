package com.nhl.link.rest.runtime.cayenne;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.EntityResolver;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.ObjectMapper;

/**
 * @since 1.7
 */
class ResourceReader {

	<T> List<T> itemsForKeys(CayenneUpdateResponse<T> response, Collection<Object> keys, ObjectMapper<T> mapper) {

		// TODO: split query in batches:
		// respect Constants.SERVER_MAX_ID_QUALIFIER_SIZE_PROPERTY
		// property of Cayenne , breaking query into subqueries.
		// Otherwise this operation will not scale.. Though I guess since we are
		// not using streaming API to read data from Cayenne, we are already
		// limited in how much data can fit in the memory map.

		List<Expression> expressions = new ArrayList<>(keys.size());
		for (Object key : keys) {

			Expression e = mapper.expressionForKey(key);
			if (e != null) {
				expressions.add(e);
			}
		}

		// no keys or all keys were for non-persistent objects
		if (expressions.isEmpty()) {
			return Collections.emptyList();
		}

		SelectQuery<T> query = SelectQuery.query(response.getType());
		query.setQualifier(ExpressionFactory.joinExp(Expression.OR, expressions));
		return response.getUpdateContext().select(query);
	}

	<T> List<T> allItems(CayenneUpdateResponse<T> response) {
		SelectQuery<T> query = SelectQuery.query(response.getType());

		// apply various request filters identifying the span of the collection

		if (response.getParent() != null) {
			EntityResolver resolver = response.getUpdateContext().getEntityResolver();
			query.andQualifier(response.getParent().qualifier(resolver));
		}

		if (response.getEntity().getQualifier() != null) {
			query.andQualifier(response.getEntity().getQualifier());
		}

		// TODO: use SelectBuilder to get Cayenne relresentaion of the resource,
		// instead of duplicating this here...

		return response.getUpdateContext().select(query);
	}
}
