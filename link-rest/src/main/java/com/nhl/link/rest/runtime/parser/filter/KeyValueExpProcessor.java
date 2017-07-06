package com.nhl.link.rest.runtime.parser.filter;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.exp.ExpressionFactory;

import javax.ws.rs.core.Response.Status;

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
		} else if (!String.class.equals(attribute.getType())) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR,
						"Invalid property type for query comparison: '" + queryProperty + "' for entity '"
								+ entity.getName() + "'");
		}
	}
}
