package io.agrest.meta;

import io.agrest.annotation.LinkType;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @since 1.18
 */
public class DefaultAgResource<T> implements AgResource<T> {

    private String path;
    private LinkType type;
    private Collection<AgOperation> operations;
    private AgEntity<T> entity;

    public DefaultAgResource() {
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
    public Collection<AgOperation> getOperations() {
        return operations;
    }

    @Override
    public AgEntity<T> getEntity() {
        return entity;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setType(LinkType type) {
        this.type = type;
    }

    public void addOperation(AgOperation operation) {
        operations.add(operation);
    }

    public void setEntity(AgEntity<T> entity) {
        this.entity = entity;
    }

}
