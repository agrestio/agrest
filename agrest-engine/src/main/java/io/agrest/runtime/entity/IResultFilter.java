package io.agrest.runtime.entity;

import io.agrest.RootResourceEntity;

/**
 * @since 4.8
 */
public interface IResultFilter {

    <T> void filterTree(RootResourceEntity<T> entity);
}
