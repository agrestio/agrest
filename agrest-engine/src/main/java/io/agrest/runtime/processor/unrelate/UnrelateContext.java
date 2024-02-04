package io.agrest.runtime.processor.unrelate;

import io.agrest.id.AgObjectId;
import io.agrest.meta.AgSchema;
import io.agrest.processor.BaseProcessingContext;
import org.apache.cayenne.di.Injector;

public class UnrelateContext<T> extends BaseProcessingContext<T> {

    private final AgSchema schema;
    private Object unresolvedSourceId;
    private AgObjectId sourceId;

    private String relationship;

    private Object unresolvedTargetId;
    private AgObjectId targetId;

    public UnrelateContext(Class<T> type, AgSchema schema, Injector injector) {
        super(type, injector);
        this.schema = schema;
    }

    /**
     * @since 5.0
     */
    public Object getUnresolvedSourceId() {
        return unresolvedSourceId;
    }

    /**
     * @since 5.0
     */
    public void setUnresolvedSourceId(Object unresolvedSourceId) {
        this.unresolvedSourceId = unresolvedSourceId;
    }

    public AgObjectId getSourceId() {
        return sourceId;
    }

    public void setSourceId(AgObjectId sourceId) {
        this.sourceId = sourceId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    /**
     * @since 5.0
     */
    public Object getUnresolvedTargetId() {
        return unresolvedTargetId;
    }

    /**
     * @since 5.0
     */
    public void setUnresolvedTargetId(Object unresolvedTargetId) {
        this.unresolvedTargetId = unresolvedTargetId;
    }

    /**
     * @since 5.0
     */
    public AgSchema getSchema() {
        return schema;
    }

    public AgObjectId getTargetId() {
        return targetId;
    }

    public void setTargetId(AgObjectId targetId) {
        this.targetId = targetId;
    }
}
