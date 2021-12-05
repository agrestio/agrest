package io.agrest.runtime.entity;

import io.agrest.AgException;
import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.runtime.processor.update.ChangeOperation;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * @since 4.8
 */
public class ChangeAuthorizer implements IChangeAuthorizer {

    @Override
    public <T> void checkCreate(List<ChangeOperation<T>> ops, CreateAuthorizer<T> authorizer) {
        if (!authorizer.allowsAll()) {
            checkRules(ops, op -> authorizer.isAllowed(op.getUpdate()));
        }
    }

    @Override
    public <T> void checkUpdate(List<ChangeOperation<T>> ops, UpdateAuthorizer<T> authorizer) {
        if (!authorizer.allowsAll()) {
            checkRules(ops, op -> authorizer.isAllowed(op.getObject(), op.getUpdate()));
        }
    }

    @Override
    public <T> void checkDelete(List<ChangeOperation<T>> ops, DeleteAuthorizer<T> authorizer) {
        if (!authorizer.allowsAll()) {
            checkRules(ops, op -> authorizer.isAllowed(op.getObject()));
        }
    }

    protected <T> void checkRules(List<ChangeOperation<T>> ops, Predicate<ChangeOperation<T>> filter) {

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

    protected <T> Object idForErrorReport(ChangeOperation<T> op) {

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
