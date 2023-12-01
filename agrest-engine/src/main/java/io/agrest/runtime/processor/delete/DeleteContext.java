package io.agrest.runtime.processor.delete;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.processor.BaseProcessingContext;
import io.agrest.runtime.EntityParent;
import io.agrest.runtime.meta.RequestSchema;
import io.agrest.runtime.processor.update.ChangeOperation;
import org.apache.cayenne.di.Injector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

    private final RequestSchema schema;
    protected AgEntity<T> agEntity;
    protected Collection<AgObjectId> ids;
    protected EntityParent<?> parent;
    private List<ChangeOperation<T>> deleteOperations;

    public DeleteContext(Class<T> type, RequestSchema schema, Injector injector) {
        super(type, injector);
        this.schema = schema;
        this.deleteOperations = Collections.emptyList();
    }

    public boolean isById() {
        return ids != null && !ids.isEmpty();
    }

    public Collection<AgObjectId> getIds() {
        return ids != null ? ids : Collections.emptyList();
    }

    /**
     * @since 5.0
     */
    public void setIds(Collection<AgObjectId> ids) {
        this.ids = ids;
    }

    /**
     * @deprecated in favor of {@link #setIds(Collection)}
     */
    @Deprecated(since = "5.0", forRemoval = true)
    public void addId(AgObjectId id) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        this.ids.add(id);
    }

    public EntityParent<?> getParent() {
        return parent;
    }

    public void setParent(EntityParent<?> parent) {
        this.parent = parent;
    }

    /**
     * @since 4.8
     */
    public void setAgEntity(AgEntity<T> agEntity) {
        this.agEntity = agEntity;
    }

    /**
     * @since 4.8
     */
    public AgEntity<T> getAgEntity() {
        return agEntity;
    }

    /**
     * @since 4.8
     */
    public List<ChangeOperation<T>> getDeleteOperations() {
        return deleteOperations;
    }

    /**
     * @since 4.8
     */
    public void setDeleteOperations(List<ChangeOperation<T>> deleteOperations) {
        this.deleteOperations = deleteOperations;
    }

    /**
     * @since 5.0
     */
    public RequestSchema getSchema() {
        return schema;
    }
}
