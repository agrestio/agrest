package com.nhl.link.rest.runtime.cayenne;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.SimpleResponse;
import com.nhl.link.rest.runtime.BaseDeleteBuilder;

/**
 * @since 1.4
 */
class CayenneDeleteBuilder<T> extends BaseDeleteBuilder<T> {

	private ICayennePersister persister;

	public CayenneDeleteBuilder(Class<T> type, ICayennePersister persister) {
		super(type);
		this.persister = persister;
	}

	@Override
	public SimpleResponse delete() {

		ObjectContext context = persister.newContext();

		// dirty ... we have no concept of DeleteResponse, and we need to
		// pass context to the ObjectMapper, so creating a throwaway response
		// and EntityUpdate .. TODO: somehow need to adapt ObjectMapper to
		// delete responses

		// delete by id
		if (id != null) {
			deleteById(context);
		}
		// delete by parent
		else if (parent != null) {
			deleteByParent(context);
		}
		// delete all !!
		else {
			deleteAll(context);
		}

		return new SimpleResponse(true);
	}

	@SuppressWarnings("unchecked")
	private void deleteById(ObjectContext context) {

		T o = Util.findById(context, type, id);

		if (o == null) {
			ObjEntity entity = context.getEntityResolver().getObjEntity(type);
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
					+ entity.getName() + "'");
		}

		context.deleteObjects(o);
		context.commitChanges();
	}

	private void deleteByParent(ObjectContext context) {

		Object parentObject = Util.findById(context, parent.getType(), parent.getId());

		if (parentObject == null) {
			ObjEntity entity = context.getEntityResolver().getObjEntity(parent.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + entity.getName() + "'");
		}

		Expression qualifier = parent.qualifier(context.getEntityResolver());
		SelectQuery<T> select = SelectQuery.query(type);
		select.andQualifier(qualifier);

		List<T> objects = context.select(select);

		context.deleteObjects(objects);
		context.commitChanges();
	}

	private void deleteAll(ObjectContext context) {
		ObjEntity e = context.getEntityResolver().getObjEntity(type);

		// TODO: is this kosher? All other deletes are done via Cayenne and
		// hence process all delete rules. This one does not
		context.performQuery(new EJBQLQuery("delete from " + e.getName()));
	}
}
