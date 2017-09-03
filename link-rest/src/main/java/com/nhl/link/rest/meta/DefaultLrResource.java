package com.nhl.link.rest.meta;

import com.nhl.link.rest.annotation.LinkType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 1.18
 */
public class DefaultLrResource<T> implements LrResource<T> {

    private String path;
    private LinkType type;
    private Collection<LrOperation> operations;
    private LrEntity<T> entity;

    public DefaultLrResource() {
        operations = new ArrayList<>();
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public LinkType getType() {
        return type;
    }

    @Override
    public Collection<LrOperation> getOperations() {
        return operations;
    }

    @Override
    public LrEntity<T> getEntity() {
        return entity;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public void addOperation(LrOperation operation) {
        operations.add(operation);
    }

    public void setEntity(LrEntity<T> entity) {
        this.entity = entity;
    }

}
