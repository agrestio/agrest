package io.agrest.runtime;

/**
 * Defines a flavor of update operation.
 */
public enum UpdateOperation {

	create, update, createOrUpdate, idempotentCreateOrUpdate, idempotentFullSync
}
