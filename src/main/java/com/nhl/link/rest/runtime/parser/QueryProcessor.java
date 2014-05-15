package com.nhl.link.rest.runtime.parser;

import java.sql.Types;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.map.ObjAttribute;
import org.apache.cayenne.map.ObjEntity;

import com.nhl.link.rest.ClientEntity;
import com.nhl.link.rest.LinkRestException;

class QueryProcessor {

	void process(ClientEntity<?> clientEntity, String query, String queryProperty) {

		if (query == null || query.length() == 0) {
			return;
		}

		if (queryProperty == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "'query' parameter is not supported by this service");
		}

		validateAttribute(clientEntity.getEntity(), queryProperty);

		query = FilterProcessor.escapeValueForLike(query) + "%";

		Expression exp = ExpressionFactory.likeIgnoreCaseExp(queryProperty, query);
		clientEntity.andQualifier(exp);
	}

	/**
	 * Checks that the user picked a valid property to compare against. Since
	 * any bad args were selected by the server-side code, return 500 response.
	 */
	private void validateAttribute(ObjEntity entity, String queryProperty) {
		ObjAttribute attribute = (ObjAttribute) entity.getAttribute(queryProperty);
		if (attribute == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "No such property '" + queryProperty
					+ "' for entity '" + entity.getName() + "'");
		}

		int jdbcType = attribute.getDbAttribute().getType();
		switch (jdbcType) {
		case Types.VARCHAR:
		case Types.CHAR:
		case Types.CLOB:
		case Types.LONGVARCHAR:
			return;
		default:
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Invalid property type for query comparison: '"
					+ queryProperty + "' for entity '" + entity.getName() + "'");
		}
	}
}
