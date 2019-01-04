package io.agrest.runtime.cayenne.processor.select;

import io.agrest.AgException;
import io.agrest.ResourceEntity;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgRelationship;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;
import io.agrest.runtime.cayenne.ICayennePersister;
import io.agrest.runtime.processor.select.SelectContext;
import org.apache.cayenne.CayenneDataObject;
import org.apache.cayenne.di.Inject;
import org.apache.cayenne.query.SelectQuery;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 2.7
 */
public class CayenneFetchDataStage implements Processor<SelectContext<?>> {

    private ICayennePersister persister;

    public CayenneFetchDataStage(@Inject ICayennePersister persister) {

        // Store persister, don't extract ObjectContext from it right away.
        // Such deferred initialization may help building POJO pipelines.

        this.persister = persister;
    }

    @Override
    public ProcessorOutcome execute(SelectContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(SelectContext<T> context) {

        List<T> objects = fetchEntity(context.getEntity());

        if (context.isAtMostOneObject() && objects.size() != 1) {

            AgEntity<?> entity = context.getEntity().getAgEntity();

            if (objects.isEmpty()) {
                throw new AgException(Response.Status.NOT_FOUND,
                        String.format("No object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            } else {
                throw new AgException(Response.Status.INTERNAL_SERVER_ERROR, String.format(
                        "Found more than one object for ID '%s' and entity '%s'", context.getId(), entity.getName()));
            }
        }
        context.setObjects(objects);
    }

    protected <T> List<T>  fetchEntity(ResourceEntity<T> resourceEntity) {
        SelectQuery<T> select = resourceEntity.getSelect();

        List<T> objects = persister.sharedContext().select(select);

        if (!resourceEntity.getChildren().isEmpty()) {
            for (Map.Entry<String, ResourceEntity<?>> e : resourceEntity.getChildren().entrySet()) {
                ResourceEntity child = e.getValue();

                List childObjects = fetchEntity(child);

                AgRelationship relationship = resourceEntity.getAgEntity().getRelationship(child.getAgEntity());
                assignChildrenToParent(objects, relationship, childObjects);
            }
        }

        resourceEntity.setResult(objects);
        return objects;
    }

    // Assigns child items to the appropriate parent item
    protected <T> List<T> assignChildrenToParent(List<T> parents, AgRelationship relationship, List children) {

        for (T parent : parents) {
            if (parent instanceof CayenneDataObject) {
                List relations = new ArrayList();

                for (Object child : children) {
                    if (child instanceof Object[]) {
                        for (Object childRelation : (Object[])child) {
                            if (childRelation.equals(parent)) {
                                relations.add(((Object[])child)[0]);
                            }
                        }
                    }
                }
                ((CayenneDataObject) parent).writePropertyDirectly(
                        relationship.getName(),
                        relationship.isToMany() ? relations : relations.isEmpty() ? null : relations.iterator().next());

            }
        }

        return parents;
    }
}
