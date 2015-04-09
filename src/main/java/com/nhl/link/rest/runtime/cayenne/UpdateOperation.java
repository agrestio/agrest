package com.nhl.link.rest.runtime.cayenne;

/**
 * Defines a flavor of update operation.
 */
enum UpdateOperation {

	create, update, createOrUpdate, idempotentCreateOrUpdate, idempotentFullSync
}
