package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.processor.Processor;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;

/**
 * @since 1.16
 */
public class CayenneDeleteStage<T> extends ProcessingStage<DeleteContext<T>, T> {

	public CayenneDeleteStage(Processor<DeleteContext<T>, ? super T> next) {
		super(next);
	}

	@Override
	protected void doExecute(DeleteContext<T> context) {

		ObjectContext cayenneContext = CayenneContextInitStage.cayenneContext(context);

		// dirty ... we have no concept of DeleteResponse, and we need to
		// pass context to the ObjectMapper, so creating a throwaway response
		// and EntityUpdate .. TODO: somehow need to adapt ObjectMapper to
		// delete responses

		// delete by id
		if (context.getId() != null) {
			deleteById(context, cayenneContext);
		}
		// delete by parent
		else if (context.getParent() != null) {
			deleteByParent(context, cayenneContext);
		}
		// delete all !!
		else {
			deleteAll(context, cayenneContext);
		}
	}

	private void deleteById(DeleteContext<?> context, ObjectContext cayenneContext) {

		Object o = Util.findById(cayenneContext, context.getType(), context.getId());

		if (o == null) {
			ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(context.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + context.getId() + "' and entity '"
					+ entity.getName() + "'");
		}

		cayenneContext.deleteObject(o);
		cayenneContext.commitChanges();
	}

	private void deleteByParent(DeleteContext<?> context, ObjectContext cayenneContext) {

		EntityParent<?> parent = context.getParent();
		Object parentObject = Util.findById(cayenneContext, parent.getType(), parent.getId());

		if (parentObject == null) {
			ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(parent.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
					+ "' and entity '" + entity.getName() + "'");
		}

		Expression qualifier = parent.qualifier(cayenneContext.getEntityResolver());
		SelectQuery<?> select = SelectQuery.query(context.getType());
		select.andQualifier(qualifier);

		List<?> objects = cayenneContext.select(select);

		cayenneContext.deleteObjects(objects);
		cayenneContext.commitChanges();
	}

	private void deleteAll(DeleteContext<?> context, ObjectContext cayenneContext) {
		ObjEntity e = cayenneContext.getEntityResolver().getObjEntity(context.getType());

		// TODO: is this kosher? All other deletes are done via Cayenne and
		// hence process all delete rules. This one does not
		cayenneContext.performQuery(new EJBQLQuery("delete from " + e.getName()));
	}

}
