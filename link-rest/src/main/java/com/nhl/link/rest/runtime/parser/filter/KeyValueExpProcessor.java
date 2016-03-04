package com.nhl.link.rest.runtime.parser.filter;

import java.sql.Types;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.meta.LrPersistentAttribute;

public class KeyValueExpProcessor implements IKeyValueExpProcessor {

	@Override
	public Expression process(LrEntity<?> entity, String queryProperty, String value) {

		if (value == null || value.length() == 0 || queryProperty == null) {
			return null;
		}

		validateAttribute(entity, queryProperty);

		value = FilterUtil.escapeValueForLike(value) + "%";

		return ExpressionFactory.likeIgnoreCaseExp(queryProperty, value);
	}

	/**
	 * Checks that the user picked a valid property to compare against. Since
	 * any bad args were selected by the server-side code, return 500 response.
	 */
	private void validateAttribute(LrEntity<?> entity, String queryProperty) {
		LrAttribute attribute = entity.getAttribute(queryProperty);
		if (attribute == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "No such property '" + queryProperty
					+ "' for entity '" + entity.getName() + "'");
		}

		// TODO: dirty dependency on LrPersistentAttribute and Cayenne ... at
		// the parser layer we should work with persistent and non-persistent
		// attributes in a uniform fashion
		if (attribute instanceof LrPersistentAttribute) {

			int jdbcType = ((LrPersistentAttribute) attribute).getJdbcType();
			switch (jdbcType) {
			case Types.VARCHAR:
			case Types.CHAR:
			case Types.CLOB:
			case Types.LONGVARCHAR:
				return;
			default:
				throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
						"Invalid property type for query comparison: '" + queryProperty + "' for entity '"
								+ entity.getName() + "'");
			}
		}
	}
}
