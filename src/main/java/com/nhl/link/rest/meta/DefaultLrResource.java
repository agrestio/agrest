package com.nhl.link.rest.meta;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultLrResource implements LrResource {

    private String path;
    private Collection<LrOperation> operations;
    private LrEntity entity;

    public DefaultLrResource() {
        operations = new ArrayList<>();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public Collection<LrOperation> getOperations() {
        return operations;
    }

    @Override
    public LrEntity getEntity() {
        return entity;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void addOperation(LrOperation operation) {
        operations.add(operation);
    }

    public void setEntity(LrEntity entity) {
        this.entity = entity;
    }

}
