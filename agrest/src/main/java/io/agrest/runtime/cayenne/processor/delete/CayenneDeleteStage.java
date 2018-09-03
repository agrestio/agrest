package io.agrest.runtime.cayenne.processor.delete;

import io.agrest.EntityParent;
import io.agrest.LinkRestException;
import io.agrest.LrObjectId;
import io.agrest.meta.LrEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.processor.Util;
import io.agrest.runtime.meta.IMetadataService;
import io.agrest.runtime.processor.delete.DeleteContext;
import org.apache.cayenne.DataObject;
import org.apache.cayenne.ObjectContext;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.exp.Expression;
import org.apache.cayenne.map.ObjEntity;
import org.apache.cayenne.query.EJBQLQuery;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @since 2.7
 */
public class CayenneDeleteStage implements Processor<DeleteContext<?>> {

    private IMetadataService metadataService;

    public CayenneDeleteStage(@Inject IMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public ProcessorOutcome execute(DeleteContext<?> context) {
        doExecute((DeleteContext<DataObject>) context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T extends DataObject> void doExecute(DeleteContext<T> context) {

        ObjectContext cayenneContext = CayenneDeleteStartStage.cayenneContext(context);

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

    private <T extends DataObject> void deleteById(DeleteContext<T> context, ObjectContext cayenneContext, LrEntity<T> lrEntity) {

        for (LrObjectId id : context.getIds()) {
            Object o = Util.findById(cayenneContext, context.getType(), lrEntity, id.get());

            if (o == null) {
                ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(context.getType());
                throw new LinkRestException(Response.Status.NOT_FOUND, "No object for ID '" + id + "' and entity '"
                        + entity.getName() + "'");
            }

            cayenneContext.deleteObject(o);
        }
        cayenneContext.commitChanges();
    }

    private <T extends DataObject> void deleteByParent(DeleteContext<T> context, ObjectContext cayenneContext, LrEntity<?> lrParentEntity) {

        EntityParent<?> parent = context.getParent();
        Object parentObject = Util.findById(cayenneContext, parent.getType(), lrParentEntity, parent.getId().get());

        if (parentObject == null) {
            ObjEntity entity = cayenneContext.getEntityResolver().getObjEntity(parent.getType());
            throw new LinkRestException(Response.Status.NOT_FOUND, "No parent object for ID '" + parent.getId()
                    + "' and entity '" + entity.getName() + "'");
        }

        Expression qualifier = parent.qualifier(cayenneContext.getEntityResolver());
        SelectQuery<?> select = SelectQuery.query(context.getType());
        select.andQualifier(qualifier);

        List<?> objects = cayenneContext.select(select);

        cayenneContext.deleteObjects(objects);
        cayenneContext.commitChanges();
    }

    private <T extends DataObject> void deleteAll(DeleteContext<?> context, ObjectContext cayenneContext) {
        ObjEntity e = cayenneContext.getEntityResolver().getObjEntity(context.getType());

        // TODO: is this kosher? All other deletes are done via Cayenne and
        // hence process all delete rules. This one does not
        cayenneContext.performQuery(new EJBQLQuery("delete from " + e.getName()));
    }
}
