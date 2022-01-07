package io.agrest.runtime.processor.unrelate;

import io.agrest.processor.BaseProcessingContext;

public class UnrelateContext<T> extends BaseProcessingContext<T> {

    private Object sourceId;
    private String relationship;
    private Object targetId;

    public UnrelateContext(Class<T> type) {
        super(type);
    }

    public Object getSourceId() {
        return sourceId;
    }

    public void setSourceId(Object sourceId) {
        this.sourceId = sourceId;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public Object getTargetId() {
        return targetId;
    }

    public void setTargetId(Object targetId) {
        this.targetId = targetId;
    }
}
