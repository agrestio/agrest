package io.agrest.runtime.processor.delete;

import io.agrest.AgObjectId;
import io.agrest.CompoundObjectId;
import io.agrest.EntityParent;
import io.agrest.SimpleObjectId;
import io.agrest.meta.AgEntity;
import io.agrest.meta.AgEntityOverlay;
import io.agrest.processor.BaseProcessingContext;
import io.agrest.runtime.processor.update.ChangeOperation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @since 1.16
 */
public class DeleteContext<T> extends BaseProcessingContext<T> {

    protected AgEntity<T> agEntity;
    protected Collection<AgObjectId> ids;
    protected EntityParent<?> parent;
    protected AgEntityOverlay<T> entityOverlay;
    private List<ChangeOperation<T>> deleteOperations;

    public DeleteContext(Class<T> type) {
        super(type);
        this.deleteOperations = Collections.emptyList();
    }

    public boolean isById() {
        return ids != null && !ids.isEmpty();
    }

    public Collection<AgObjectId> getIds() {
        return ids;
    }

    public void addId(Object id) {
        if (ids == null) {
            ids = new ArrayList<>();
        }
        ids.add(new SimpleObjectId(id));
    }

    public void addCompoundId(Map<String, Object> ids) {
        if (this.ids == null) {
            this.ids = new ArrayList<>();
        }
        this.ids.add(new CompoundObjectId(ids));
    }

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
    public void addEntityOverlay(AgEntityOverlay<T> overlay) {
        AgEntityOverlay<T> base = this.entityOverlay != null ? this.entityOverlay : new AgEntityOverlay<>(getType());
        this.entityOverlay = base.merge(overlay);
    }

    /**
     * @since 4.8
     */
    public AgEntityOverlay<T> getEntityOverlay() {
        return entityOverlay;
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
}
