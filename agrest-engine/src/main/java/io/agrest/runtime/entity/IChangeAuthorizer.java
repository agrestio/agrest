package io.agrest.runtime.entity;

import io.agrest.access.CreateAuthorizer;
import io.agrest.access.DeleteAuthorizer;
import io.agrest.access.UpdateAuthorizer;
import io.agrest.runtime.processor.update.ChangeOperation;

import java.util.List;

/**
 * @since 4.8
 */
public interface IChangeAuthorizer {

    <T> void checkCreate(List<ChangeOperation<T>> ops, CreateAuthorizer<T> authorizer);

    <T> void checkUpdate(List<ChangeOperation<T>> ops, UpdateAuthorizer<T> authorizer);

    <T> void checkDelete(List<ChangeOperation<T>> ops, DeleteAuthorizer<T> authorizer);
}
