package com.nhl.link.rest.runtime;

/**
 * Defines a flavor of update operation.
 */
public enum UpdateOperation {

	create, update, createOrUpdate, idempotentCreateOrUpdate, idempotentFullSync
}
