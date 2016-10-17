package com.nhl.link.rest.runtime.cayenne.processor;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.meta.LrAttribute;
import com.nhl.link.rest.meta.LrEntity;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.ExpressionFactory;
import org.apache.cayenne.exp.parser.ASTEqual;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.ObjectSelect;

import javax.ws.rs.core.Response.Status;
import java.util.Map;

/**
 * @since 1.7
 */
class Util {

	private Util() {
	}

	@SuppressWarnings("unchecked")
	static <A> A findById(ObjectContext context, Class<A> type, LrEntity<?> lrEntity, Object id) {
		ObjEntity entity = context.getEntityResolver().getObjEntity(type);

		// sanity checking...
		if (entity == null) {
			throw new LinkRestException(Status.INTERNAL_SERVER_ERROR, "Unknown entity class: " + type);
		}

		if (id == null) {
			throw new LinkRestException(Status.BAD_REQUEST, "No id specified");
		}

		if (id instanceof Map) {
			Map<String, Object> ids = (Map<String, Object>) id;
			ObjectSelect<A> query = ObjectSelect.query(type);
			for (Map.Entry<String, Object> entry : ids.entrySet()) {
				query.and(ExpressionFactory.matchDbExp(
						entity.getDbEntity().getAttribute(entry.getKey()).getName(), entry.getValue()
				));
			}
			return query.selectOne(context);
		} else {
			LrAttribute attribute = lrEntity.getIds().iterator().next();
			return ObjectSelect.query(type, new ASTEqual(attribute.getPathExp(), id)).selectOne(context);
		}
	}
}
