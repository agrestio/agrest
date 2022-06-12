package io.agrest.runtime.processor.unrelate;

import io.agrest.id.AgObjectId;
import io.agrest.processor.BaseProcessingContext;
import org.apache.cayenne.di.Injector;

public class UnrelateContext<T> extends BaseProcessingContext<T> {

    private AgObjectId sourceId;
    private String relationship;
    private AgObjectId targetId;

    public UnrelateContext(Class<T> type, Injector injector) {
        super(type, injector);
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

    public AgObjectId getTargetId() {
        return targetId;
    }

    public void setTargetId(AgObjectId targetId) {
        this.targetId = targetId;
    }
}
