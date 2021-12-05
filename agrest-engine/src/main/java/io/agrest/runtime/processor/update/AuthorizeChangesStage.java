package io.agrest.runtime.processor.update;

import io.agrest.AgException;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.meta.AgEntity;
import io.agrest.processor.Processor;
import io.agrest.processor.ProcessorOutcome;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A processor associated with {@link io.agrest.UpdateStage#AUTHORIZE_CHANGES} that runs change authorization filters
 * against request data change operations. It would fail the chain if at least one rule is not satisfied.
 *
 * @since 4.8
 */
public class AuthorizeChangesStage implements Processor<UpdateContext<?>> {

    @Override
    public ProcessorOutcome execute(UpdateContext<?> context) {
        doExecute(context);
        return ProcessorOutcome.CONTINUE;
    }

    protected <T> void doExecute(UpdateContext<T> context) {

        AgEntity<T> entity = context.getEntity().getAgEntity();

        CreateAuthorizer<T> createAuthorizer = entity.getCreateAuthorizer();
        if (!createAuthorizer.allowsAll()) {
            checkRules(
                    context.getChangeOperations().get(ChangeOperationType.CREATE),
                    op -> createAuthorizer.isAllowed(op.getUpdate()));
        }

        UpdateAuthorizer<T> updateAuthorizer = entity.getUpdateAuthorizer();
        if (!updateAuthorizer.allowsAll()) {
            checkRules(
                    context.getChangeOperations().get(ChangeOperationType.UPDATE),
                    op -> updateAuthorizer.isAllowed(op.getObject(), op.getUpdate()));
        }

        DeleteAuthorizer<T> deleteAuthorizer = entity.getDeleteAuthorizer();
        if (!deleteAuthorizer.allowsAll()) {
            checkRules(
                    context.getChangeOperations().get(ChangeOperationType.DELETE),
                    op -> deleteAuthorizer.isAllowed(op.getObject()));
        }
    }

    static <T> void checkRules(
            List<ChangeOperation<T>> ops,
            Predicate<ChangeOperation<T>> filter) {

        for (ChangeOperation<T> op : ops) {
            if (!filter.test(op)) {

                Object id = idForErrorReport(op);

                throw AgException.forbidden("%s of %s%s was blocked by authorization rules",
                        op.getType(),
                        op.getEntity().getName(),
                        id == null ? "" : " with id of " + id);
            }
        }
    }

    static <T> Object idForErrorReport(ChangeOperation<T> op) {

        // different operations provide different source

        if (op.getUpdate() != null) {
            Map<String, Object> updateId = op.getUpdate().getId();
            if (updateId != null) {
                return updateId.size() == 1 ? updateId.values().iterator().next() : updateId;
            }
        }

        if (op.getObject() != null) {
            Object id = op.getEntity().getIdReader().value(op.getObject());
            return id instanceof Map && ((Map) id).size() == 1 ? ((Map) id).values().iterator().next() : id;
        }

        return null;
    }
}
