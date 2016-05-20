package com.nhl.link.rest.runtime.cayenne.processor;

import java.util.List;

import javax.ws.rs.core.Response.Status;

import com.nhl.link.rest.meta.LrEntity;
import com.nhl.link.rest.runtime.meta.IMetadataService;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SelectQuery;

import com.nhl.link.rest.EntityParent;
import com.nhl.link.rest.LinkRestException;
import com.nhl.link.rest.processor.BaseLinearProcessingStage;
import com.nhl.link.rest.processor.ProcessingStage;
import com.nhl.link.rest.runtime.processor.delete.DeleteContext;

/**
 * @since 1.16
 */
public class CayenneDeleteStage<T> extends BaseLinearProcessingStage<DeleteContext<T>, T> {

	private IMetadataService metadataService;

	public CayenneDeleteStage(ProcessingStage<DeleteContext<T>, ? super T> next, IMetadataService metadataService) {
		super(next);
		this.metadataService = metadataService;
	}

	@Override
	protected void doExecute(DeleteContext<T> context) {

		ObjectContext cayenneContext = CayenneContextInitStage.cayenneContext(context);

		// dirty ... we have no concept of DeleteResponse, and we need to
		// pass context to the ObjectMapper, so creating a throwaway response
		// and EntityUpdate .. TODO: somehow need to adapt ObjectMapper to
		// delete responses

		// delete by id
		if (context.isById()) {
			LrEntity<T> lrEntity = metadataService.getLrEntity(context.getType());
			deleteById(context, cayenneContext, lrEntity);
		}
		// delete by parent
		else if (context.getParent() != null) {
			LrEntity<?> parentLrEntity = metadataService.getLrEntity(context.getParent().getType());
			deleteByParent(context, cayenneContext, parentLrEntity);
		}
		// delete all !!
		else {
			deleteAll(context, cayenneContext);
		}
	}

	private void deleteById(DeleteContext<T> context, ObjectContext cayenneContext, LrEntity<T> lrEntity) {

		Object o = Util.findById(cayenneContext, context.getType(), lrEntity, context.getId().get());

		if (o == null) {
			ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(context.getType());
			throw new LinkRestException(Status.NOT_FOUND, "No object for ID '" + context.getId() + "' and entity '"
					+ entity.getName() + "'");
		}

		cayenneContext.deleteObject(o);
		cayenneContext.commitChanges();
	}

	private void deleteByParent(DeleteContext<T> context, ObjectContext cayenneContext, LrEntity<?> lrParentEntity) {

		EntityParent<?> parent = context.getParent();
		Object parentObject = Util.findById(cayenneContext, parent.getType(), lrParentEntity, parent.getId().get());

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
